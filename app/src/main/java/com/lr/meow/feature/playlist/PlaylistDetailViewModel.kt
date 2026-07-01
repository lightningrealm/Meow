package com.lr.meow.feature.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowPlaylistService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class PlaylistDetailViewModel(
    private val playlistService: MeowPlaylistService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.LoadPlaylist -> loadPlaylist(intent.id)
            is PlaylistDetailIntent.PlaySong -> { /* TODO: Implement with core-player */ }
            is PlaylistDetailIntent.PlayAll -> { /* TODO: Implement with core-player */ }
        }
    }

    private fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null, playlistDetail = null, songs = emptyList()) }
            try {
                // Fetch detail and tracks concurrently
                val detailDeferred = async { playlistService.getPlaylistDetail(id) }
                val tracksDeferred = async { playlistService.getPlaylistTracks(id, limit = 500) } // Assuming 500 is enough for a playlist

                val detailResponse = detailDeferred.await()
                val tracksResponse = tracksDeferred.await()

                if (detailResponse.code == 200 && tracksResponse.code == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playlistDetail = detailResponse.playlist,
                            songs = tracksResponse.songs ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Failed to load playlist: Code ${detailResponse.code} / ${tracksResponse.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetailViewModel", "Error loading playlist", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}
