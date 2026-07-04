package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistTopSongsResponse(
    @SerialName("code") val code: Int,
    @SerialName("songs") val songs: List<Song>? = null
)

@Serializable
data class ArtistAlbumsResponse(
    @SerialName("code") val code: Int,
    @SerialName("artist") val artist: Artist? = null,
    @SerialName("hotAlbums") val hotAlbums: List<Album>? = null
)

@Serializable
data class ArtistSongsResponse(
    @SerialName("code") val code: Int? = null,
    @SerialName("total") val total: Int = 0,
    @SerialName("songs") val songs: List<Song>? = null
)