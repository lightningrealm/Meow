package com.lr.meow.feature.album

import com.lr.core.network.model.AlbumDetail
import com.lr.core.network.model.Song
import com.lr.meow.ui.common.util.UiText

data class AlbumDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: UiText? = null,
    val albumDetail: AlbumDetail? = null,
    val songs: List<Song> = emptyList()
)

sealed class AlbumDetailIntent {
    data class LoadAlbum(val id: Long) : AlbumDetailIntent()
    data class PlaySong(val song: Song) : AlbumDetailIntent()
    object PlayAll : AlbumDetailIntent()
}
