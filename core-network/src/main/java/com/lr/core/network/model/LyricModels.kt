package com.lr.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricResponse(
    val lrc: Lrc? = null,
    val yrc: Yrc? = null,
    val tlyric: Lrc? = null,
    val romalrc: Lrc? = null,
    val code: Int
)

@Serializable
data class NewLyricResponse(
    val lrc: Lrc? = null,
    val yrc: Yrc? = null,
    val tlyric: Lrc? = null,
    val romalrc: Lrc? = null,
    val code: Int
)

@Serializable
data class Lrc(
    val version: Int = 0,
    val lyric: String = ""
)

@Serializable
data class Yrc(
    val version: Int = 0,
    val lyric: String = ""
)
