package com.lr.meow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowRecommendService
import com.lr.core.network.model.RecommendPlaylist
import com.lr.core.network.model.Song
import com.lr.meow.R
import com.lr.meow.ui.common.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.toKotlinDuration

data class HomeUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val needsLogin: Boolean = false,
    val errorMessage: UiText? = null,
    val todayDate: String = "",
    val recommendPlaylists: List<RecommendPlaylist> = emptyList(),
    val recommendSongs: List<Song> = emptyList()
)

class HomeViewModel(
    private val recommendService: MeowRecommendService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        startDateTicker()
    }

    private fun startDateTicker(){
        viewModelScope.launch {
            while (true){
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("M月d日 EEE", Locale.CHINESE)
                val fullDateStr = now.format(formatter)
                _uiState.value = _uiState.value.copy(
                    todayDate = fullDateStr
                )
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
                delay(Duration.between(now,nextMidnight).toKotlinDuration())
            }
        }
    }

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
            } catch (_: Exception) {
                // Ignore or handle dislike error
            }
        }
    }
}
