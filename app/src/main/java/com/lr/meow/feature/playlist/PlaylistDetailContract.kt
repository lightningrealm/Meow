package com.lr.meow.feature.playlist

import com.lr.core.network.model.PlaylistDetail
import com.lr.core.network.model.RecommendSong

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val playlistDetail: PlaylistDetail? = null,
    val songs: List<RecommendSong> = emptyList()
)

sealed class PlaylistDetailIntent {
    data class LoadPlaylist(val id: Long) : PlaylistDetailIntent()
    data class PlaySong(val song: RecommendSong) : PlaylistDetailIntent()
    object PlayAll : PlaylistDetailIntent()
}
