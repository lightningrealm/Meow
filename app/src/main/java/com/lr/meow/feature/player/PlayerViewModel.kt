package com.lr.meow.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowSongService
import com.lr.core.network.model.Song
import com.lr.core_player.MusicController
import com.lr.meow.feature.player.mapper.toMediaItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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
