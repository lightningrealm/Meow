package com.lr.core.network

import com.lr.core.model.Playlist
import com.lr.core.model.Song
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Define your API endpoints here.
 * This is an example setup for a generic music API.
 */
interface MeowApiService {
    
    @GET("api/v1/songs/trending")
    suspend fun getTrendingSongs(
        @Query("limit") limit: Int = 20
    ): List<Song>

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetails(
        @Path("id") playlistId: String
    ): Playlist

    @GET("api/v1/search")
    suspend fun searchSongs(
        @Query("q") query: String
    ): List<Song>
}
