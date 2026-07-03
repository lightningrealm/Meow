package com.lr.core.network.api

import com.lr.core.network.model.AnonymousLoginResponse
import com.lr.core.network.model.BaseResponse
import com.lr.core.network.model.CaptchaSentResponse
import com.lr.core.network.model.LoginResponse
import com.lr.core.network.model.LoginStatusResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.lr.core.network.model.QrKeyResponse
import com.lr.core.network.model.QrCreateResponse
import com.lr.core.network.model.QrCheckResponse
import com.lr.core.network.model.RefreshLoginResponse

interface MeowAuthService {
    
    /**
     * 发送验证码
     */
    @GET("captcha/sent")
    suspend fun sendCaptcha(
        @Query("phone") phone: String,
        @Query("ctcode") ctcode: String = "86"
    ): BaseResponse<Boolean>

    /**
     * 验证验证码是否正确 (非强制，登录本身会验证)
     */
    @GET("captcha/verify")
    suspend fun verifyCaptcha(
        @Query("phone") phone: String,
        @Query("captcha") captcha: String,
        @Query("ctcode") ctcode: String = "86"
    ): BaseResponse<Boolean>

    /**
     * 手机号+验证码登录
     */
    @GET("login/cellphone")
    suspend fun loginWithCaptcha(
        @Query("phone") phone: String,
        @Query("captcha") captcha: String,
        @Query("countrycode") countrycode: String = "86"
    ): LoginResponse

    /**
     * 刷新登录状态
     */
    @GET("login/refresh")
    suspend fun refreshLogin(): RefreshLoginResponse
    
    /**
     * 获取登录状态
     */
    @GET("login/status")
    suspend fun checkLoginStatus(): LoginStatusResponse
    

    /**
     * 游客登录
     */
    @GET("register/anonimous")
    suspend fun anonymousLogin(): AnonymousLoginResponse

    /**
     * 生成二维码 key
     */
    @GET("login/qr/key")
    suspend fun getQrKey(
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrKeyResponse

    /**
     * 生成二维码 base64
     */
    @GET("login/qr/create")
    suspend fun createQrCode(
        @Query("key") key: String,
        @Query("qrimg") qrimg: Boolean = true,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCreateResponse

    /**
     * 轮询二维码扫码状态
     */
    @GET("login/qr/check")
    suspend fun checkQrStatus(
        @Query("key") key: String,
        @Query("noCookie") noCookie: Boolean = true,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCheckResponse
}
