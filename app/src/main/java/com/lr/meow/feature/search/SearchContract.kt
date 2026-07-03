package com.lr.meow.feature.search

import com.lr.core.network.model.HotSearchItem
import com.lr.core.network.model.SearchPlaylist
import com.lr.core.network.model.SearchSong
import com.lr.core.network.model.SearchSuggestMatch
import com.lr.meow.R
import com.lr.meow.ui.common.util.UiText

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: UiText? = null,
    
    val searchState: SearchState = SearchState.Idle,
    
    // Idle state
    val searchHistory: List<String> = emptyList(),
    val hotSearches: List<HotSearchItem> = emptyList(),
    
    // Typing state
    val suggestions: List<SearchSuggestMatch> = emptyList(),
    
    // Result state
    val activeTab: SearchTab = SearchTab.SONGS,
    val songResults: List<SearchSong> = emptyList(),
    val playlistResults: List<SearchPlaylist> = emptyList(),
    // Pagination (simplified for now)
    val hasMore: Boolean = false
)

enum class SearchState {
    Idle,      // Not searching, shows hot searches & history
    Typing,    // Currently typing, shows suggestions
    Results    // Submitted, shows search results
}

enum class SearchTab(val typeCode: Int, val titleResId: Int) {
    SONGS(1, R.string.songs),
    PLAYLISTS(1000, R.string.playlist)
}

sealed class SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent()
    object SubmitSearch : SearchIntent()
    data class ChangeTab(val tab: SearchTab) : SearchIntent()
    data class SelectSuggestion(val keyword: String) : SearchIntent()
    data class ClickHistory(val keyword: String) : SearchIntent()
    object ClearHistory : SearchIntent()
    object LoadMore : SearchIntent()
}
