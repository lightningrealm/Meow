package com.lr.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * 网易云 API 通用返回格式
 */
data class BaseResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("data")
    val data: T?
)

/**
 * 发送验证码的结果
 */
data class CaptchaSentResponse(
    @SerializedName("data")
    val data: Boolean?
)

/**
 * 登录结果
 */
data class LoginResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("cookie")
    val cookie: String?,
    @SerializedName("token")
    val token: String?
)
