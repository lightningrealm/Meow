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

@Serializable
data class QrKeyResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("data") val data: QrKeyData? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class QrKeyData(
    @SerialName("unikey") val unikey: String = ""
)

@Serializable
data class QrCreateResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("data") val data: QrCreateData? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class QrCreateData(
    @SerialName("qrurl") val qrurl: String = "",
    @SerialName("qrimg") val qrimg: String = ""
)

@Serializable
data class QrCheckResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("message") val message: String? = null,
    @SerialName("cookie") val cookie: String? = null
)

@Serializable
data class AnonymousLoginResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("userId") val userId: Long? = null,
    @SerialName("createTime") val createTime: Long? = null,
    @SerialName("cookie") val cookie: String? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class RefreshLoginResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("bizCode") val bizCode: String? = null,
    @SerialName("cookie") val cookie: String? = null,
    @SerialName("message") val message: String? = null
)
