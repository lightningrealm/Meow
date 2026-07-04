package com.lr.core_player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicController(private val context: Context) {

    private var controller: MediaController? = null

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    suspend fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        try {
            controller = MediaController.Builder(context, sessionToken).buildAsync().await()
            controller?.let {
                it.addListener(playerListener)
                syncInitialState(it)
            }
            Log.d("MusicController","后台音乐服务连接成功")
        } catch (e: Exception) {
            Log.d("MusicController","后台音乐服务连接失败",e)
        }
    }

    private fun syncInitialState(controller: MediaController) {
        _playbackState.value = controller.playbackState
        _isPlaying.value = controller.isPlaying
        _currentMediaItem.value = controller.currentMediaItem
        updateProgress()
    }

    fun release() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }

    fun playMediaItems(items: List<MediaItem>, startIndex: Int = 0) {
        controller?.let {
            it.setMediaItems(items, startIndex, 0L)
            it.prepare()
            it.play()
        }
    }

    fun addMediaItems(items: List<MediaItem>) {
        controller?.addMediaItems(items)
    }

    val currentMediaItemIndex: Int
        get() = controller?.currentMediaItemIndex ?: 0

    val mediaItemCount: Int
        get() = controller?.mediaItemCount ?: 0

    fun play() {
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun skipToNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    // Call this from a UI ticker (e.g. LaunchedEffect loop) to keep progress updated
    fun updateProgress() {
        controller?.let {
            _currentPosition.value = it.currentPosition
            if (it.duration >= 0) {
                _duration.value = it.duration
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.value = state
            updateProgress()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            updateProgress()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = mediaItem
            updateProgress()
        }
    }
}
