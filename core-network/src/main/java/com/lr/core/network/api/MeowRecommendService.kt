package com.lr.core.network.api

import com.lr.core.network.model.DislikeSongResponse
import com.lr.core.network.model.PersonalFmResponse
import com.lr.core.network.model.RecommendResourceResponse
import com.lr.core.network.model.RecommendSongsResponse
import com.lr.core.network.model.ToplistResponse
import com.lr.core.network.model.NewestAlbumResponse
import com.lr.core.network.model.TopArtistsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeowRecommendService {

    @GET("/recommend/resource")
    suspend fun getRecommendPlaylists(): RecommendResourceResponse

    @GET("/recommend/songs")
    suspend fun getRecommendSongs(): RecommendSongsResponse

    @GET("/personal_fm")
    suspend fun getPersonalFm(
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): PersonalFmResponse

    @GET("/recommend/songs/dislike")
    suspend fun dislikeSong(
        @Query("id") id: Long,
        @Query("alg") alg: String = "itembased"
    ): DislikeSongResponse

    @GET("/toplist")
    suspend fun getToplist(): ToplistResponse

    @GET("/album/newest")
    suspend fun getNewestAlbums(): NewestAlbumResponse

    @GET("/top/artists")
    suspend fun getTopArtists(
        @Query("limit") limit: Int = 10
    ): TopArtistsResponse
}
