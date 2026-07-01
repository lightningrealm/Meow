package com.lr.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * 基础用户配置/资料信息
 */
data class UserProfile(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("backgroundUrl")
    val backgroundUrl: String?,
    @SerializedName("signature")
    val signature: String?,
    @SerializedName("followeds")
    val followeds: Int?, // 粉丝数
    @SerializedName("follows")
    val follows: Int?, // 关注数
    @SerializedName("eventCount")
    val eventCount: Int? // 动态数
)

/**
 * /user/account 接口的响应
 */
data class UserAccountResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("profile")
    val profile: UserProfile?,
    @SerializedName("account")
    val account: AccountInfo?
) {
    data class AccountInfo(
        @SerializedName("id")
        val id: Long,
        @SerializedName("userName")
        val userName: String?,
        @SerializedName("createTime")
        val createTime: Long?,
        @SerializedName("status")
        val status: Int?
    )
}

/**
 * /user/detail 接口的响应
 */
data class UserDetailResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("level")
    val level: Int?,
    @SerializedName("listenSongs")
    val listenSongs: Int?,
    @SerializedName("profile")
    val profile: UserProfile?,
    @SerializedName("createDays")
    val createDays: Int?
)

/**
 * /user/subcount 接口的响应
 */
data class UserSubcountResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("programCount")
    val programCount: Int?,
    @SerializedName("djRadioCount")
    val djRadioCount: Int?,
    @SerializedName("mvCount")
    val mvCount: Int?,
    @SerializedName("artistCount")
    val artistCount: Int?,
    @SerializedName("newProgramCount")
    val newProgramCount: Int?,
    @SerializedName("createDjRadioCount")
    val createDjRadioCount: Int?,
    @SerializedName("createdPlaylistCount")
    val createdPlaylistCount: Int?,
    @SerializedName("subPlaylistCount")
    val subPlaylistCount: Int?
)

/**
 * /user/level 接口的响应
 */
data class UserLevelResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val data: LevelData?
) {
    data class LevelData(
        @SerializedName("level")
        val level: Int,
        @SerializedName("progress")
        val progress: Double,
        @SerializedName("nextPlayCount")
        val nextPlayCount: Int,
        @SerializedName("nextLoginCount")
        val nextLoginCount: Int,
        @SerializedName("nowPlayCount")
        val nowPlayCount: Int,
        @SerializedName("nowLoginCount")
        val nowLoginCount: Int
    )
}
