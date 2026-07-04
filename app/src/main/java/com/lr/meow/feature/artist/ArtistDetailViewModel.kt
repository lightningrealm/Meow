package com.lr.meow.feature.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowSongService
import com.lr.meow.ui.common.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtistDetailViewModel(
    private val service: MeowSongService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistDetailState())
    val uiState: StateFlow<ArtistDetailState> = _uiState.asStateFlow()

    fun handleIntent(intent: ArtistDetailIntent) {
        when (intent) {
            is ArtistDetailIntent.LoadArtist -> fetchArtistDetails(intent.id)
            is ArtistDetailIntent.ChangeTab -> _uiState.update { it.copy(selectedTabIndex = intent.index) }
        }
    }

    private fun fetchArtistDetails(id: Long) {
        _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Launch concurrent requests
                val albumsDeferred = async { service.getArtistAlbums(id, limit = 30) }
                val topSongsDeferred = async { service.getArtistTopSongs(id) }
                val allSongsDeferred = async { service.getArtistSongs(id, limit = 50) }

                val albumsResponse = albumsDeferred.await()
                val topSongsResponse = topSongsDeferred.await()
                val allSongsResponse = allSongsDeferred.await()

                if (albumsResponse.code == 200 && topSongsResponse.code == 200 && (allSongsResponse.code == 200 || allSongsResponse.code == null)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = false,
                            artistInfo = albumsResponse.artist,
                            hotAlbums = albumsResponse.hotAlbums ?: emptyList(),
                            topSongs = topSongsResponse.songs ?: emptyList(),
                            allSongs = allSongsResponse.songs ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = UiText.DynamicString("API returned error codes")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = UiText.DynamicString(e.message ?: "Unknown Error")
                    )
                }
            }
        }
    }
}