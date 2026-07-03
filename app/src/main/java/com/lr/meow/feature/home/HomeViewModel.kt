package com.lr.meow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowRecommendService
import com.lr.core.network.model.RecommendPlaylist
import com.lr.core.network.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.lr.meow.ui.common.util.UiText
import com.lr.meow.R

data class HomeUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val needsLogin: Boolean = false,
    val errorMessage: UiText? = null,
    val recommendPlaylists: List<RecommendPlaylist> = emptyList(),
    val recommendSongs: List<Song> = emptyList(),
    val personalFmSongs: List<Song> = emptyList()
)

class HomeViewModel(
    private val recommendService: MeowRecommendService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchRecommendSongs() {
        if (_uiState.value.recommendSongs.isNotEmpty()) return // Already fetched
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)
            try {
                val response = recommendService.getRecommendSongs()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recommendSongs = response.data?.dailySongs ?: emptyList()
                )
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    fun fetchPersonalFm() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)
            try {
                val response = recommendService.getPersonalFm()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    personalFmSongs = _uiState.value.personalFmSongs + (response.data ?: emptyList())
                )
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    fun popFmSong() {
        val currentSongs = _uiState.value.personalFmSongs
        if (currentSongs.isNotEmpty()) {
            val newList = currentSongs.drop(1)
            _uiState.value = _uiState.value.copy(personalFmSongs = newList)
            if (newList.isEmpty()) {
                fetchPersonalFm()
            }
        }
    }

    private fun handleNetworkError(e: Exception) {
        val msg = e.message ?: ""
        val isAuthError = msg.contains("HTTP 301") || msg.contains("HTTP 401") || msg.contains("HTTP 404")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isError = true,
            needsLogin = isAuthError,
            errorMessage = if (isAuthError) {
                UiText.StringResource(R.string.login_expired)
            } else {
                e.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.load_failed)
            }
        )
    }

    fun dislikeSong(songId: Long) {
        viewModelScope.launch {
            try {
                val response = recommendService.dislikeSong(songId)
                val newSong = response.data
                
                if (newSong != null) {
                    val currentSongs = _uiState.value.recommendSongs.toMutableList()
                    val index = currentSongs.indexOfFirst { it.id == songId }
                    if (index != -1) {
                        currentSongs[index] = newSong
                        _uiState.value = _uiState.value.copy(recommendSongs = currentSongs)
                    }
                } else {
                    val currentSongs = _uiState.value.recommendSongs.filter { it.id != songId }
                    _uiState.value = _uiState.value.copy(recommendSongs = currentSongs)
                }
            } catch (e: Exception) {
                // Ignore or handle dislike error
            }
        }
    }
}
