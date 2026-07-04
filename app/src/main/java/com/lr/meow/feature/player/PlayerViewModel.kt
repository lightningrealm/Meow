package com.lr.meow.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.lr.core.network.api.MeowRecommendService
import com.lr.core.network.api.MeowSongService
import com.lr.core.network.model.Song
import com.lr.core_player.MusicController
import com.lr.meow.feature.player.mapper.toMediaItem
import com.lr.meow.feature.player.model.LyricLine
import com.lr.meow.feature.player.model.parseLrc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed interface LyricUiState {
    data object Loading : LyricUiState
    data class Success(val lyrics: List<LyricLine>) : LyricUiState
    data class Error(val message: String) : LyricUiState
}

enum class PlaybackMode {
    NORMAL,
    FM
}

class PlayerViewModel(
    private val musicController: MusicController,
    private val songService: MeowSongService,
    private val recommendService: MeowRecommendService
) : ViewModel() {

    val playbackState = musicController.playbackState
    val isPlaying = musicController.isPlaying
    val currentMediaItem = musicController.currentMediaItem
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    private val _lyricState = MutableStateFlow<LyricUiState>(LyricUiState.Loading)
    val lyricState: StateFlow<LyricUiState> = _lyricState.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.NORMAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    // Expose the current FM queue for the UI cards
    private val _fmFullQueue = MutableStateFlow<List<Song>>(emptyList())
    private val _fmQueue = MutableStateFlow<List<Song>>(emptyList())
    val fmQueue: StateFlow<List<Song>> = _fmQueue.asStateFlow()
    
    private fun updateFmDisplayQueue() {
        val fullQueue = _fmFullQueue.value
        if (_playbackMode.value == PlaybackMode.FM) {
            val currentIndex = musicController.currentMediaItemIndex
            if (currentIndex < fullQueue.size) {
                _fmQueue.value = fullQueue.drop(currentIndex)
            } else {
                _fmQueue.value = emptyList()
            }
            
            // Pre-fetch more songs when we are close to the end
            if (currentIndex >= fullQueue.size - 2 && !_isFetchingFm.value) {
                fetchMoreFm()
            }
        } else {
            // In NORMAL mode, just show the full preview queue
            _fmQueue.value = fullQueue
        }
    }

    fun loadInitialFmSongs() {
        if (_fmFullQueue.value.isNotEmpty() || _isFetchingFm.value) return
        _isFetchingFm.value = true
        viewModelScope.launch {
            try {
                val response = recommendService.getPersonalFm()
                val newSongs = response.data ?: emptyList()
                if (newSongs.isNotEmpty()) {
                    _fmFullQueue.value = newSongs
                    updateFmDisplayQueue()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingFm.value = false
            }
        }
    }

    private val _isFetchingFm = MutableStateFlow(false)
    val isFetchingFm: StateFlow<Boolean> = _isFetchingFm.asStateFlow()

    init {
        viewModelScope.launch {
            musicController.initialize()
        }

        viewModelScope.launch {
            playbackState.collect { state ->
                if (state == Player.STATE_ENDED && _playbackMode.value == PlaybackMode.FM) {
                    fetchMoreFm()
                }
            }
        }
        
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    musicController.updateProgress()
                }
                delay(500.milliseconds)
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

                    // Handle FM queue replenishing
                    if (_playbackMode.value == PlaybackMode.FM) {
                        updateFmDisplayQueue()
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
        _playbackMode.value = PlaybackMode.NORMAL
        _fmQueue.value = emptyList()
        viewModelScope.launch {
            try {
                val ids = songs.joinToString(",") { it.id.toString() }
                val urlResponse = songService.getSongUrl(id = ids)
                val urlData = urlResponse.data
                if (urlResponse.code == 200 && urlData != null) {
                    val urlMap = urlData.associateBy { it.id }
                    val mediaItems = songs.mapNotNull { song ->
                        val urlInfo = urlMap[song.id]
                        val url = urlInfo?.url?.replace("http://", "https://")
                        if (url != null) {
                            song.toMediaItem(url)
                        } else null
                    }
                    if (mediaItems.isNotEmpty()) {
                        musicController.playMediaItems(mediaItems, startIndex.coerceIn(0, mediaItems.size - 1))
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun playFm() {
        val initialSongs = _fmFullQueue.value
        if (initialSongs.isEmpty()) return
        
        _playbackMode.value = PlaybackMode.FM
        updateFmDisplayQueue()
        viewModelScope.launch {
            try {
                val ids = initialSongs.joinToString(",") { it.id.toString() }
                val urlResponse = songService.getSongUrl(id = ids)
                val urlData = urlResponse.data
                if (urlResponse.code == 200 && urlData != null) {
                    val urlMap = urlData.associateBy { it.id }
                    val mediaItems = initialSongs.mapNotNull { song ->
                        val urlInfo = urlMap[song.id]
                        val url = urlInfo?.url?.replace("http://", "https://")
                        if (url != null) {
                            song.toMediaItem(url)
                        } else null
                    }
                    if (mediaItems.isNotEmpty()) {
                        musicController.playMediaItems(mediaItems, 0)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchMoreFm() {
        if (_isFetchingFm.value) return
        _isFetchingFm.value = true
        viewModelScope.launch {
            try {
                val response = recommendService.getPersonalFm()
                val newSongs = response.data ?: emptyList()
                if (newSongs.isNotEmpty()) {
                    val ids = newSongs.joinToString(",") { it.id.toString() }
                    val urlResponse = songService.getSongUrl(id = ids)
                    val urlData = urlResponse.data
                    if (urlResponse.code == 200 && urlData != null) {
                        val urlMap = urlData.associateBy { it.id }
                        val mediaItems = newSongs.mapNotNull { song ->
                            val urlInfo = urlMap[song.id]
                            val url = urlInfo?.url?.replace("http://", "https://")
                            if (url != null) {
                                song.toMediaItem(url)
                            } else null
                        }
                        if (mediaItems.isNotEmpty()) {
                            val wasEnded = playbackState.value == Player.STATE_ENDED
                            val shouldSkip = wasEnded || isWaitingForFmSkip
                            musicController.addMediaItems(mediaItems)
                            if (shouldSkip) {
                                musicController.skipToNext()
                                musicController.play()
                                isWaitingForFmSkip = false
                            }
                            
                            // It's important to update the UI queue state, appending the new raw songs.
                            // However, we only append songs that were successfully mapped to MediaItems.
                            val successfullyAddedSongs = newSongs.filter { song -> urlMap[song.id]?.url != null }
                            _fmFullQueue.update { it + successfullyAddedSongs }
                            updateFmDisplayQueue()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingFm.value = false
            }
        }
    }

    private var isWaitingForFmSkip = false

    fun play() = musicController.play()
    fun pause() = musicController.pause()
    fun skipToNext() {
        if (_playbackMode.value == PlaybackMode.FM && musicController.currentMediaItemIndex >= musicController.mediaItemCount - 1) {
            // We are skipping the last item. Fetch more and wait for them to be added.
            isWaitingForFmSkip = true
            fetchMoreFm()
            return
        }
        musicController.skipToNext()
    }
    fun skipToPrevious() = musicController.skipToPrevious()
    fun seekTo(positionMs: Long) = musicController.seekTo(positionMs)
    fun updateProgress() = musicController.updateProgress()
}
