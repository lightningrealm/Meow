package com.lr.core.network.api

import com.lr.core.network.model.BaseResponse
import com.lr.core.network.model.CaptchaSentResponse
import com.lr.core.network.model.LoginResponse
import retrofit2.http.GET
import retrofit2.http.Query

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
    suspend fun refreshLogin(): BaseResponse<Any>
    
    /**
     * 获取登录状态
     */
    @GET("login/status")
    suspend fun checkLoginStatus(): com.lr.core.network.model.LoginStatusResponse
    
    /**
     * 游客登录
     */
    @GET("register/anonimous")
    suspend fun anonymousLogin(): BaseResponse<Any>
}
