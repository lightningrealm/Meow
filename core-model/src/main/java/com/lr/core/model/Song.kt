package com.lr.core.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val mediaUrl: String,
    val durationMs: Long
)
