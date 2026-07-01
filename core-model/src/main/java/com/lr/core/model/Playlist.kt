package com.lr.core.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val tracks: List<Song>
)
