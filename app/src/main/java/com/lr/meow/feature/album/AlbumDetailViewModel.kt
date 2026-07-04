package com.lr.meow.feature.album

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowSongService
import com.lr.meow.R
import com.lr.meow.ui.common.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val songService: MeowSongService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: AlbumDetailIntent) {
        when (intent) {
            is AlbumDetailIntent.LoadAlbum -> loadAlbum(intent.id)
            is AlbumDetailIntent.PlaySong -> { /* Delegated to PlayerViewModel in UI */ }
            is AlbumDetailIntent.PlayAll -> { /* Delegated to PlayerViewModel in UI */ }
        }
    }

    private fun loadAlbum(id: Long) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    isError = false, 
                    errorMessage = null, 
                    albumDetail = null, 
                    songs = emptyList()
                ) 
            }
            try {
                val response = songService.getAlbum(id)
                if (response.code == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            albumDetail = response.album,
                            songs = response.songs ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = UiText.DynamicString("Failed to load album: Code ${response.code}")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AlbumDetailViewModel", "Error loading album", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.load_failed)
                    )
                }
            }
        }
    }
}
