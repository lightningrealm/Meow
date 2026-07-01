package com.lr.core.network.api

import com.lr.core.network.model.PlaylistDetailResponse
import com.lr.core.network.model.PlaylistTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeowPlaylistService {
    @GET("/playlist/detail")
    suspend fun getPlaylistDetail(
        @Query("id") id: Long
    ): PlaylistDetailResponse

    @GET("/playlist/track/all")
    suspend fun getPlaylistTracks(
        @Query("id") id: Long,
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0
    ): PlaylistTracksResponse
}
