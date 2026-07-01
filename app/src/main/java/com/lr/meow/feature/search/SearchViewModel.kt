package com.lr.meow.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.datastore.SearchHistoryStorage
import com.lr.core.network.api.MeowSearchService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchService: MeowSearchService,
    private val searchHistoryStorage: SearchHistoryStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        // Observe history
        viewModelScope.launch {
            searchHistoryStorage.historyFlow.collect { history ->
                _uiState.update { it.copy(searchHistory = history) }
            }
        }

        // Fetch hot searches on init
        fetchHotSearches()

        // Setup debounce for typing suggestions
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank() && _uiState.value.searchState == SearchState.Typing) {
                        fetchSuggestions(query)
                    }
                }
        }
    }

    fun dispatch(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> handleUpdateQuery(intent.query)
            is SearchIntent.SubmitSearch -> submitSearch()
            is SearchIntent.ChangeTab -> handleChangeTab(intent.tab)
            is SearchIntent.SelectSuggestion -> {
                handleUpdateQuery(intent.keyword)
                submitSearch()
            }
            is SearchIntent.ClickHistory -> {
                handleUpdateQuery(intent.keyword)
                submitSearch()
            }
            is SearchIntent.ClearHistory -> clearHistory()
            is SearchIntent.LoadMore -> loadMoreResults()
        }
    }

    private fun handleUpdateQuery(query: String) {
        val state = if (query.isBlank()) SearchState.Idle else SearchState.Typing
        _uiState.update { 
            it.copy(
                query = query, 
                searchState = state,
                suggestions = if (query.isBlank()) emptyList() else it.suggestions
            )
        }
        queryFlow.value = query
    }

    private fun submitSearch() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        _uiState.update { it.copy(searchState = SearchState.Results) }
        
        // Save history
        viewModelScope.launch {
            searchHistoryStorage.addSearchQuery(query)
        }

        // Perform actual search
        performSearch(query, _uiState.value.activeTab, offset = 0)
    }

    private fun handleChangeTab(tab: SearchTab) {
        if (_uiState.value.activeTab == tab) return
        _uiState.update { it.copy(activeTab = tab) }
        val query = _uiState.value.query
        if (query.isNotBlank() && _uiState.value.searchState == SearchState.Results) {
            performSearch(query, tab, offset = 0)
        }
    }

    private fun fetchHotSearches() {
        viewModelScope.launch {
            try {
                val response = searchService.getHotSearches()
                if (response.code == 200) {
                    _uiState.update { it.copy(hotSearches = response.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                // Ignore hot search error gracefully
            }
        }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val response = searchService.getSearchSuggest(query)
                if (response.code == 200) {
                    _uiState.update { it.copy(suggestions = response.result?.allMatch ?: emptyList()) }
                }
            } catch (e: Exception) {
                // Ignore suggestion error gracefully
            }
        }
    }

    private fun performSearch(query: String, tab: SearchTab, offset: Int) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }
            try {
                val response = searchService.search(
                    keywords = query,
                    type = tab.typeCode,
                    offset = offset
                )
                if (response.code == 200) {
                    val result = response.result
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            songResults = if (tab == SearchTab.SONGS) result?.songs ?: emptyList() else state.songResults,
                            playlistResults = if (tab == SearchTab.PLAYLISTS) result?.playlists ?: emptyList() else state.playlistResults
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isError = true, errorMessage = "Search failed code ${response.code}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isError = true, errorMessage = e.message) }
            }
        }
    }

    private fun loadMoreResults() {
        // Simplified pagination for now
    }

    private fun clearHistory() {
        viewModelScope.launch {
            searchHistoryStorage.clearHistory()
        }
    }
}
