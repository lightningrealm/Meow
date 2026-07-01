package com.lr.core.network.model

import com.google.gson.annotations.SerializedName

data class PlaylistResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("more")
    val more: Boolean,
    @SerializedName("playlist")
    val playlist: List<Playlist>?
)

data class Playlist(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("coverImgUrl")
    val coverImgUrl: String?,
    @SerializedName("trackCount")
    val trackCount: Int,
    @SerializedName("playCount")
    val playCount: Long?,
    @SerializedName("creator")
    val creator: Creator?
)

data class Creator(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("nickname")
    val nickname: String?
)
