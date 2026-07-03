package com.lr.meow.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowSongService
import com.lr.core.network.model.Song
import com.lr.core_player.MusicController
import com.lr.meow.feature.player.mapper.toMediaItem
import com.lr.meow.feature.player.model.LyricLine
import com.lr.meow.feature.player.model.parseLrc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed interface LyricUiState {
    data object Loading : LyricUiState
    data class Success(val lyrics: List<LyricLine>) : LyricUiState
    data class Error(val message: String) : LyricUiState
}

class PlayerViewModel(
    private val musicController: MusicController,
    private val songService: MeowSongService
) : ViewModel() {

    val playbackState = musicController.playbackState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1) // Player.STATE_IDLE

    val isPlaying = musicController.isPlaying
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentMediaItem = musicController.currentMediaItem
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPosition = musicController.currentPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val duration = musicController.duration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    private val _lyricState = MutableStateFlow<LyricUiState>(LyricUiState.Loading)
    val lyricState: StateFlow<LyricUiState> = _lyricState.asStateFlow()

    init {
        musicController.initialize()
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    musicController.updateProgress()
                }
                kotlinx.coroutines.delay(500.milliseconds)
            }
        }
        
        viewModelScope.launch {
            currentMediaItem.collect { mediaItem ->
                if (mediaItem != null) {
                    _lyricState.value = LyricUiState.Loading
                    try {
                        val songId = mediaItem.mediaId
                        val response = songService.getLyric(songId)
                        if (response.code == 200) {
                            val parsed = parseLrc(response.lrc?.lyric)
                            _lyricState.value = LyricUiState.Success(parsed)
                        } else {
                            _lyricState.value = LyricUiState.Error("获取歌词失败")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _lyricState.value = LyricUiState.Error(e.message ?: "未知错误")
                    }
                } else {
                    _lyricState.value = LyricUiState.Success(emptyList())
                }
            }
        }
    }

    override fun onCleared() {
        musicController.release()
        super.onCleared()
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                // For a real app, you might want to fetch URLs lazily or in batches
                // For simplicity here, we fetch the first one and set up the playlist.
                // A better approach is writing a custom MediaSource, but Media3 can take the URI.
                // We'll fetch the URLs before passing to MediaController.
                val ids = songs.joinToString(",") { it.id.toString() }
                
                // Note: Get URL from service
                val urlResponse = songService.getSongUrl(id = ids)
                val urlData = urlResponse.data
                if (urlResponse.code == 200 && urlData != null) {
                    val urlMap = urlData.associateBy { it.id }
                    
                    val mediaItems = songs.mapNotNull { song ->
                        val urlInfo = urlMap[song.id]
                        val url = urlInfo?.url?.replace("http://", "https://")
                        if (url != null) {
                            song.toMediaItem(url)
                        } else {
                            null
                        }
                    }
                    
                    if (mediaItems.isNotEmpty()) {
                        musicController.playMediaItems(mediaItems, startIndex.coerceIn(0, mediaItems.size - 1))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun play() = musicController.play()
    fun pause() = musicController.pause()
    fun skipToNext() = musicController.skipToNext()
    fun skipToPrevious() = musicController.skipToPrevious()
    fun seekTo(positionMs: Long) = musicController.seekTo(positionMs)
    fun updateProgress() = musicController.updateProgress()
}
