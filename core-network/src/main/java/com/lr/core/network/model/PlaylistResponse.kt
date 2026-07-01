package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("playlist")
    val playlist: List<Playlist>? = null
)

@Serializable
data class Playlist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("coverImgUrl")
    val coverImgUrl: String? = null,
    @SerialName("trackCount")
    val trackCount: Int = 0,
    @SerialName("playCount")
    val playCount: Long? = null,
    @SerialName("creator")
    val creator: Creator? = null
)

@Serializable
data class Creator(
    @SerialName("userId")
    val userId: Long,
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("avatarUrl")
    val avatarUrl: String? = null
)

@Serializable
data class PlaylistDetailResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("playlist")
    val playlist: PlaylistDetail? = null
)

@Serializable
data class PlaylistDetail(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("coverImgUrl")
    val coverImgUrl: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("trackCount")
    val trackCount: Int = 0,
    @SerialName("playCount")
    val playCount: Long? = null,
    @SerialName("creator")
    val creator: Creator? = null
)

@Serializable
data class PlaylistTracksResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("songs")
    val songs: List<RecommendSong>? = null
)
