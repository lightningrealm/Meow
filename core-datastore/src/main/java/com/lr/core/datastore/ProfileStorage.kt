package com.lr.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "user_profile_prefs")

data class CachedProfile(
    val userId: Long? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val level: Int? = null,
    val listenSongs: Int? = null,
    val followerCount: Int? = null,
    val followingCount: Int? = null,
    val playlistCount: Int? = null
)

class ProfileStorage(private val context: Context) {

    companion object {
        private val KEY_USER_ID = androidx.datastore.preferences.core.longPreferencesKey("userId")
        private val KEY_NICKNAME = stringPreferencesKey("nickname")
        private val KEY_AVATAR = stringPreferencesKey("avatarUrl")
        private val KEY_BACKGROUND = stringPreferencesKey("backgroundUrl")
        private val KEY_LEVEL = intPreferencesKey("level")
        private val KEY_LISTEN_SONGS = intPreferencesKey("listenSongs")
        private val KEY_FOLLOWERS = intPreferencesKey("followerCount")
        private val KEY_FOLLOWING = intPreferencesKey("followingCount")
        private val KEY_PLAYLISTS = intPreferencesKey("playlistCount")
        private val KEY_PLAYLISTS_JSON = stringPreferencesKey("playlists_json")
    }

    val profileFlow: Flow<CachedProfile> = context.profileDataStore.data.map { prefs ->
        CachedProfile(
            userId = prefs[KEY_USER_ID],
            nickname = prefs[KEY_NICKNAME],
            avatarUrl = prefs[KEY_AVATAR],
            backgroundUrl = prefs[KEY_BACKGROUND],
            level = prefs[KEY_LEVEL],
            listenSongs = prefs[KEY_LISTEN_SONGS],
            followerCount = prefs[KEY_FOLLOWERS],
            followingCount = prefs[KEY_FOLLOWING],
            playlistCount = prefs[KEY_PLAYLISTS]
        )
    }

    val playlistsJsonFlow: Flow<String?> = context.profileDataStore.data.map { prefs ->
        prefs[KEY_PLAYLISTS_JSON]
    }

    suspend fun saveProfile(profile: CachedProfile) {
        context.profileDataStore.edit { prefs ->
            profile.userId?.let { prefs[KEY_USER_ID] = it }
            profile.nickname?.let { prefs[KEY_NICKNAME] = it }
            profile.avatarUrl?.let { prefs[KEY_AVATAR] = it }
            profile.backgroundUrl?.let { prefs[KEY_BACKGROUND] = it }
            profile.level?.let { prefs[KEY_LEVEL] = it }
            profile.listenSongs?.let { prefs[KEY_LISTEN_SONGS] = it }
            profile.followerCount?.let { prefs[KEY_FOLLOWERS] = it }
            profile.followingCount?.let { prefs[KEY_FOLLOWING] = it }
            profile.playlistCount?.let { prefs[KEY_PLAYLISTS] = it }
        }
    }

    suspend fun savePlaylistsJson(json: String) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_PLAYLISTS_JSON] = json
        }
    }

    suspend fun clearProfile() {
        context.profileDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
