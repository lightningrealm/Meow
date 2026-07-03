package com.lr.meow.feature.player.mapper

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lr.core.network.model.Song

fun Song.toMediaItem(url: String): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.id.toString())
        .setUri(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.name)
                .setArtist(this.artistName)
                .setAlbumTitle(this.al?.name)
                .setArtworkUri(android.net.Uri.parse(this.al?.picUrl ?: ""))
                .build()
        )
        .build()
}
