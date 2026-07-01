package com.lr.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 网易云 API 通用返回格式
 */
@Serializable
data class BaseResponse<T>(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String? = null,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: T? = null
)

/**
 * 发送验证码的结果
 */
@Serializable
data class CaptchaSentResponse(
    @SerialName("data")
    val data: Boolean? = null
)

/**
 * 登录结果
 */
@Serializable
data class LoginResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("cookie")
    val cookie: String? = null,
    @SerialName("token")
    val token: String? = null
)

/**
 * 登录状态返回
 */
@Serializable
data class LoginStatusResponse(
    @SerialName("data")
    val data: LoginStatusData? = null
)

@Serializable
data class LoginStatusData(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("account")
    val account: Account? = null
)

@Serializable
data class Account(
    @SerialName("id")
    val id: Long? = null
)
