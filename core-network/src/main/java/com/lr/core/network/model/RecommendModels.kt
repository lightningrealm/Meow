@file:OptIn(ExperimentalSerializationApi::class)
package com.lr.core.network.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.util.Locale

@Serializable
data class RecommendResourceResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("recommend")
    val recommend: List<RecommendPlaylist>? = null
)

@Serializable
data class RecommendPlaylist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("picUrl")
    val picUrl: String? = null,
    @SerialName("copywriter")
    val copywriter: String? = null,
    @SerialName("playcount")
    val playcount: Long = 0,
    @SerialName("trackCount")
    val trackCount: Int = 0,
    @SerialName("creator")
    val creator: Creator? = null
)

@Serializable
data class RecommendSongsResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: RecommendSongsData? = null
)

@Serializable
data class RecommendSongsData(
    @SerialName("dailySongs")
    val dailySongs: List<Song>? = null
)

@Serializable
data class PersonalFmResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: List<Song>? = null
)

@Serializable
data class DislikeSongResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: Song? = null
)

@Serializable
data class Song(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String? = null,
    @SerialName("ar")
    @JsonNames("artists")
    val ar: List<Artist>? = null,
    @SerialName("al")
    @JsonNames("album")
    val al: Album? = null,
    @SerialName("reason")
    val reason: String? = null
) {
    val artistName: String
        get() {
            val fallback = if (Locale.getDefault().language == "zh") "未知歌手" else "Unknown Artist"
            return ar?.joinToString("/") { it.name ?: fallback } ?: fallback
        }
}

@Serializable
data class Artist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String? = null,
    @SerialName("picUrl")
    val picUrl: String? = null
)

@Serializable
data class Album(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String? = null,
    @SerialName("picUrl")
    val picUrl: String? = null,
    @SerialName("artist")
    val artist: Artist? = null
)

@Serializable
data class ToplistResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("list")
    val list: List<Toplist>? = null
)

@Serializable
data class Toplist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("coverImgUrl")
    val coverImgUrl: String? = null,
    @SerialName("updateFrequency")
    val updateFrequency: String? = null
)

@Serializable
data class NewestAlbumResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("albums")
    val albums: List<Album>? = null
)

@Serializable
data class TopArtistsResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("artists")
    val artists: List<Artist>? = null
)
