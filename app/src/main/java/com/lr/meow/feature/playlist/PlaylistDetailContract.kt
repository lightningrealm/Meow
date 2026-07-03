package com.lr.meow.feature.playlist

import com.lr.core.network.model.PlaylistDetail
import com.lr.core.network.model.Song
import com.lr.meow.ui.common.util.UiText

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: UiText? = null,
    val playlistDetail: PlaylistDetail? = null,
    val songs: List<Song> = emptyList()
)

sealed class PlaylistDetailIntent {
    data class LoadPlaylist(val id: Long) : PlaylistDetailIntent()
    data class PlaySong(val song: Song) : PlaylistDetailIntent()
    object PlayAll : PlaylistDetailIntent()
}
