package com.lr.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

val Context.dataStore by preferencesDataStore(name = "meow_cookies")

class CookieStorage(private val context: Context) {

    init {
        // Initialize Tink when CookieStorage is created
        TinkManager.init(context)
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        // Simplified check: if we have cookies for lightningrealm.cloud, we consider it logged in.
        val encryptedCookies = preferences[stringPreferencesKey("cookie_www.lightningrealm.cloud")]
        val cookies = encryptedCookies?.let { TinkManager.decrypt(it) } ?: ""
        cookies.contains("MUSIC_U") || cookies.isNotEmpty()
    }.distinctUntilChanged()

    fun getCookies(domain: String): Flow<String?> {
        val key = stringPreferencesKey("cookie_$domain")
        return context.dataStore.data.map { preferences ->
            val encryptedCookies = preferences[key]
            if (encryptedCookies != null) {
                TinkManager.decrypt(encryptedCookies)
            } else {
                null
            }
        }
    }

    suspend fun saveCookies(domain: String, cookies: String) {
        val key = stringPreferencesKey("cookie_$domain")
        val encryptedCookies = TinkManager.encrypt(cookies)
        context.dataStore.edit { preferences ->
            preferences[key] = encryptedCookies
        }
    }
    
    suspend fun clearCookies(domain: String) {
        val key = stringPreferencesKey("cookie_$domain")
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
