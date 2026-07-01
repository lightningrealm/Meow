package com.lr.core.network.api

import com.lr.core.network.model.PlaylistResponse
import com.lr.core.network.model.UserAccountResponse
import com.lr.core.network.model.UserDetailResponse
import com.lr.core.network.model.UserLevelResponse
import com.lr.core.network.model.UserSubcountResponse
import com.lr.core.network.model.RecentSongResponse
import com.lr.core.network.model.RecentPlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeowUserService {
    
    /**
     * 获取用户详情
     * 说明 : 登录后调用此接口 , 传入用户 id, 可以获取用户详情
     * @param uid 用户 id
     */
    @GET("user/detail")
    suspend fun getUserDetail(@Query("uid") uid: Long): UserDetailResponse

    /**
     * 获取账号信息
     * 说明 : 登录后调用此接口 ,可获取用户账号信息
     */
    @GET("user/account")
    suspend fun getUserAccount(): UserAccountResponse

    /**
     * 获取用户信息 , 歌单，收藏，mv, dj 数量
     * 说明 : 登录后调用此接口 , 可以获取用户信息
     */
    @GET("user/subcount")
    suspend fun getUserSubcount(): UserSubcountResponse

    /**
     * 获取用户等级信息
     * 说明 : 登录后调用此接口 , 可以获取用户等级信息,包含当前登录天数,听歌次数...
     */
    @GET("user/level")
    suspend fun getUserLevel(): UserLevelResponse

    @GET("/user/playlist")
    suspend fun getUserPlaylists(
        @Query("uid") uid: Long,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): PlaylistResponse

    @GET("/record/recent/song")
    suspend fun getRecentSongs(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): RecentSongResponse

    @GET("/record/recent/playlist")
    suspend fun getRecentPlaylists(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): RecentPlaylistResponse
}
