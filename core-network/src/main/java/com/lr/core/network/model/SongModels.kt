package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongUrlResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: List<SongUrlData>? = null
)

@Serializable
data class SongUrlData(
    @SerialName("id")
    val id: Long,
    @SerialName("url")
    val url: String? = null,
    @SerialName("br")
    val br: Int = 0,
    @SerialName("size")
    val size: Long = 0,
    @SerialName("md5")
    val md5: String? = null,
    @SerialName("time")
    val time: Long = 0,
    @SerialName("level")
    val level: String? = null,
    @SerialName("freeTrialInfo")
    val freeTrialInfo: FreeTrialInfo? = null
)

@Serializable
data class FreeTrialInfo(
    @SerialName("start")
    val start: Long,
    @SerialName("end")
    val end: Long
)

@Serializable
data class SongDetailResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("songs")
    val songs: List<Song>? = null
)
