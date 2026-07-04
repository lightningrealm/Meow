package com.lr.core.network.api

import com.lr.core.network.model.SongDetailResponse
import com.lr.core.network.model.SongUrlResponse
import com.lr.core.network.model.LyricResponse
import com.lr.core.network.model.NewLyricResponse
import com.lr.core.network.model.AlbumDetailResponse
import com.lr.core.network.model.LikeResponse
import com.lr.core.network.model.LikelistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeowSongService {
    @GET("/song/url/v1")
    suspend fun getSongUrl(
        @Query("id") id: String,
        @Query("level") level: String = "exhigh"
    ): SongUrlResponse

    @GET("/song/detail")
    suspend fun getSongDetail(
        @Query("ids") ids: String
    ): SongDetailResponse

    @GET("/lyric")
    suspend fun getLyric(
        @Query("id") id: String
    ): LyricResponse

    @GET("/lyric/new")
    suspend fun getNewLyric(
        @Query("id") id: String
    ): NewLyricResponse

    @GET("/like")
    suspend fun likeSong(
        @Query("id") id: Long,
        @Query("like") like: Boolean = true
    ): LikeResponse

    @GET("/likelist")
    suspend fun getLikelist(
        @Query("uid") uid: Long
    ): LikelistResponse

    @GET("/album")
    suspend fun getAlbum(
        @Query("id") id: Long
    ): AlbumDetailResponse
}
