package com.lr.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

object AuthManager {
    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents = _authEvents.asSharedFlow()

    fun triggerLogout() {
        _authEvents.tryEmit(AuthEvent.LoggedOut)
    }
}

sealed class AuthEvent {
    object LoggedOut : AuthEvent()
}

class AuthInterceptor(private val cookieJar: PersistentCookieJar) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // Cookie expired or invalid
            val domain = request.url.host
            cookieJar.clearCookies(domain)
            
            // Broadcast event globally so UI can navigate to Login Screen
            AuthManager.triggerLogout()
        }

        return response
    }
}
