package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 基础用户配置/资料信息
 */
@Serializable
data class UserProfile(
    @SerialName("userId")
    val userId: Long,
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("avatarUrl")
    val avatarUrl: String? = null,
    @SerialName("backgroundUrl")
    val backgroundUrl: String? = null,
    @SerialName("signature")
    val signature: String? = null,
    @SerialName("followeds")
    val followeds: Int? = null, // 粉丝数
    @SerialName("follows")
    val follows: Int? = null, // 关注数
    @SerialName("eventCount")
    val eventCount: Int? = null // 动态数
)

/**
 * /user/account 接口的响应
 */
@Serializable
data class UserAccountResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("profile")
    val profile: UserProfile? = null,
    @SerialName("account")
    val account: AccountInfo? = null
) {
    @Serializable
    data class AccountInfo(
        @SerialName("id")
        val id: Long,
        @SerialName("userName")
        val userName: String? = null,
        @SerialName("createTime")
        val createTime: Long? = null,
        @SerialName("status")
        val status: Int? = null
    )
}

/**
 * /user/detail 接口的响应
 */
@Serializable
data class UserDetailResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("level")
    val level: Int? = null,
    @SerialName("listenSongs")
    val listenSongs: Int? = null,
    @SerialName("profile")
    val profile: UserProfile? = null,
    @SerialName("createDays")
    val createDays: Int? = null
)

/**
 * /user/subcount 接口的响应
 */
@Serializable
data class UserSubcountResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("programCount")
    val programCount: Int? = null,
    @SerialName("djRadioCount")
    val djRadioCount: Int? = null,
    @SerialName("mvCount")
    val mvCount: Int? = null,
    @SerialName("artistCount")
    val artistCount: Int? = null,
    @SerialName("newProgramCount")
    val newProgramCount: Int? = null,
    @SerialName("createDjRadioCount")
    val createDjRadioCount: Int? = null,
    @SerialName("createdPlaylistCount")
    val createdPlaylistCount: Int? = null,
    @SerialName("subPlaylistCount")
    val subPlaylistCount: Int? = null
)

/**
 * /user/level 接口的响应
 */
@Serializable
data class UserLevelResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: LevelData? = null
) {
    @Serializable
    data class LevelData(
        @SerialName("level")
        val level: Int,
        @SerialName("progress")
        val progress: Double,
        @SerialName("nextPlayCount")
        val nextPlayCount: Int,
        @SerialName("nextLoginCount")
        val nextLoginCount: Int,
        @SerialName("nowPlayCount")
        val nowPlayCount: Int,
        @SerialName("nowLoginCount")
        val nowLoginCount: Int
    )
}

@Serializable
data class RecentSongResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: RecentSongData? = null
)

@Serializable
data class RecentSongData(
    @SerialName("list")
    val list: List<RecentSongItem>? = null
)

@Serializable
data class RecentSongItem(
    @SerialName("playTime")
    val playTime: Long,
    @SerialName("data")
    val song: RecommendSong
)

@Serializable
data class RecentPlaylistResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: RecentPlaylistData? = null
)

@Serializable
data class RecentPlaylistData(
    @SerialName("list")
    val list: List<RecentPlaylistItem>? = null
)

@Serializable
data class RecentPlaylistItem(
    @SerialName("playTime")
    val playTime: Long,
    @SerialName("data")
    val playlist: Playlist
)
