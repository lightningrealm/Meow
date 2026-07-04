package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. Hot Search (Detail)
@Serializable
data class HotSearchDetailResponse(
    @SerialName("code") val code: Int,
    @SerialName("data") val data: List<HotSearchItem>? = null
)

@Serializable
data class HotSearchItem(
    @SerialName("searchWord") val searchWord: String,
    @SerialName("score") val score: Int = 0,
    @SerialName("content") val content: String? = null,
    @SerialName("source") val source: Int = 0,
    @SerialName("iconType") val iconType: Int = 0,
    @SerialName("iconUrl") val iconUrl: String? = null
)

// 2. Search Suggest
@Serializable
data class SearchSuggestResponse(
    @SerialName("code") val code: Int,
    @SerialName("result") val result: SearchSuggestResult? = null
)

@Serializable
data class SearchSuggestResult(
    @SerialName("allMatch") val allMatch: List<SearchSuggestMatch>? = null
)

@Serializable
data class SearchSuggestMatch(
    @SerialName("keyword") val keyword: String,
    @SerialName("type") val type: Int = 0,
    @SerialName("alg") val alg: String? = null,
    @SerialName("lastKeyword") val lastKeyword: String? = null
)

// 3. Cloud Search
@Serializable
data class CloudSearchResponse(
    @SerialName("code") val code: Int,
    @SerialName("result") val result: CloudSearchResult? = null
)

@Serializable
data class CloudSearchResult(
    @SerialName("songCount") val songCount: Int? = null,
    @SerialName("songs") val songs: List<SearchSong>? = null,
    @SerialName("playlistCount") val playlistCount: Int? = null,
    @SerialName("playlists") val playlists: List<SearchPlaylist>? = null
)

@Serializable
data class SearchSong(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("ar") val ar: List<Artist>? = null,
    @SerialName("al") val al: Album? = null,
    @SerialName("dt") val duration: Long? = null
) {
    val artistName: String
        get() {
            val fallback = if (java.util.Locale.getDefault().language == "zh") "未知歌手" else "Unknown Artist"
            return ar?.joinToString("/") { it.name ?: fallback } ?: fallback
        }
        
    fun toSong(): Song {
        return Song(
            id = id,
            name = name,
            ar = ar,
            al = al
        )
    }
}

@Serializable
data class SearchPlaylist(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("coverImgUrl") val coverImgUrl: String? = null,
    @SerialName("creator") val creator: Creator? = null,
    @SerialName("trackCount") val trackCount: Int = 0,
    @SerialName("playCount") val playCount: Long = 0
)
