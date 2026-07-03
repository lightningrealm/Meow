package com.lr.meow.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowRecommendService
import com.lr.core.network.model.RecommendPlaylist
import com.lr.core.network.model.RecommendSong
import com.lr.core.network.model.Toplist
import com.lr.core.network.model.Album
import com.lr.core.network.model.Artist
import com.lr.meow.R
import com.lr.meow.ui.common.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val needsLogin: Boolean = false,
    val errorMessage: UiText? = null,
    val recommendPlaylists: List<RecommendPlaylist> = emptyList(),
    val toplists: List<Toplist> = emptyList(),
    val newAlbums: List<Album> = emptyList(),
    val topArtists: List<Artist> = emptyList()
)

class DiscoverViewModel(
    private val recommendService: MeowRecommendService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    fun fetchRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)
            try {
                kotlinx.coroutines.coroutineScope {
                    val playlistsDeferred = async { recommendService.getRecommendPlaylists() }
                    val toplistDeferred = async { recommendService.getToplist() }
                    val newAlbumsDeferred = async { recommendService.getNewestAlbums() }
                    val topArtistsDeferred = async { recommendService.getTopArtists() }

                    val playlistsRes = playlistsDeferred.await()
                    val toplistRes = toplistDeferred.await()
                    val newAlbumsRes = newAlbumsDeferred.await()
                    val topArtistsRes = topArtistsDeferred.await()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendPlaylists = playlistsRes.recommend ?: emptyList(),
                        toplists = toplistRes.list ?: emptyList(),
                        newAlbums = newAlbumsRes.albums ?: emptyList(),
                        topArtists = topArtistsRes.artists ?: emptyList()
                    )
                }
            } catch (e: Exception) {
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
        }
    }

}
