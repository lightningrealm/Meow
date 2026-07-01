package com.lr.core.network

import com.lr.core.datastore.CookieStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(private val cookieStorage: CookieStorage) : CookieJar {

    // Simple in-memory cache to avoid reading from DataStore on every single request
    private val memoryCookies = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host
        
        // Update memory cache
        val currentCookies = memoryCookies[domain] ?: mutableListOf()
        val newCookiesMap = currentCookies.associateBy { it.name }.toMutableMap()
        for (cookie in cookies) {
            newCookiesMap[cookie.name] = cookie
        }
        val updatedCookies = newCookiesMap.values.toList()
        memoryCookies[domain] = updatedCookies.toMutableList()

        // Persist to DataStore synchronously (OkHttp background thread)
        val cookieString = updatedCookies.joinToString(";") { "${it.name}=${it.value}" }
        runBlocking {
            cookieStorage.saveCookies(domain, cookieString)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val domain = url.host

        // 1. Try memory cache
        memoryCookies[domain]?.let {
            return it
        }

        // 2. Fallback to DataStore
        val cookieString = runBlocking {
            cookieStorage.getCookies(domain).firstOrNull()
        }

        if (cookieString.isNullOrEmpty()) {
            return emptyList()
        }

        // Parse cookies from string (simplified parsing, assuming standard Name=Value format)
        val cookies = cookieString.split(";").mapNotNull {
            val parts = it.trim().split("=", limit = 2)
            if (parts.size == 2) {
                Cookie.Builder()
                    .domain(domain)
                    .name(parts[0])
                    .value(parts[1])
                    .build()
            } else null
        }
        
        memoryCookies[domain] = cookies.toMutableList()
        return cookies
    }
    
    fun clearCookies(domain: String) {
        memoryCookies.remove(domain)
        runBlocking {
            cookieStorage.clearCookies(domain)
        }
    }
}
