package com.lr.meow.feature.artist

import com.lr.core.network.model.Album
import com.lr.core.network.model.Artist
import com.lr.core.network.model.Song
import com.lr.meow.ui.common.util.UiText

data class ArtistDetailState(
    val artistInfo: Artist? = null,
    val topSongs: List<Song> = emptyList(),
    val hotAlbums: List<Album> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    
    val selectedTabIndex: Int = 0,
    
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: UiText? = null
)

sealed interface ArtistDetailIntent {
    data class LoadArtist(val id: Long) : ArtistDetailIntent
    data class ChangeTab(val index: Int) : ArtistDetailIntent
}
