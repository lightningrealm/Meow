package com.lr.meow.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lr.core.datastore.CachedProfile
import com.lr.core.datastore.CookieStorage
import com.lr.core.datastore.ProfileStorage
import com.lr.core.network.api.MeowUserService
import com.lr.core.network.model.Playlist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class ProfileViewModel(
    private val userService: MeowUserService,
    private val cookieStorage: CookieStorage,
    private val profileStorage: ProfileStorage
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _currentUid = MutableStateFlow<Long?>(null)

    // Combine loading states with cached profile flow
    val uiState: StateFlow<ProfileUiState> = combine(
        _isLoading,
        _isError,
        _errorMessage,
        profileStorage.profileFlow
    ) { isLoading, isError, errorMessage, cachedProfile ->
        ProfileUiState(
            isLoading = isLoading,
            isError = isError,
            errorMessage = errorMessage,
            userProfile = if (cachedProfile.nickname != null) {
                com.lr.core.network.model.UserProfile(
                    userId = 0, // Ignored for display
                    nickname = cachedProfile.nickname,
                    avatarUrl = cachedProfile.avatarUrl,
                    backgroundUrl = cachedProfile.backgroundUrl,
                    signature = null,
                    followeds = cachedProfile.followerCount,
                    follows = cachedProfile.followingCount,
                    eventCount = null
                )
            } else null,
            userLevel = cachedProfile.level,
            listenSongs = cachedProfile.listenSongs,
            playlistCount = cachedProfile.playlistCount,
            followerCount = cachedProfile.followerCount,
            followingCount = cachedProfile.followingCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    private val gson = Gson()
    
    val currentUid: StateFlow<Long?> = _currentUid.asStateFlow()

    val playlistsFlow: StateFlow<List<Playlist>> = profileStorage.playlistsJsonFlow.map { json ->
        if (json != null) {
            val type = object : TypeToken<List<Playlist>>() {}.type
            gson.fromJson<List<Playlist>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun dispatch(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.RefreshProfile -> fetchProfileData()
        }
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            try {
                // To fetch details, we first need to know the UID.
                // The /user/account endpoint tells us the currently logged-in user's profile and ID.
                val accountResponse = userService.getUserAccount()
                val uid = accountResponse.profile?.userId

                if (uid == null) {
                    _isLoading.value = false
                    _isError.value = true
                    _errorMessage.value = "未获取到用户ID，请重新登录"
                    return@launch
                }
                
                _currentUid.value = uid

                // Now we can fetch details, level, subcount and playlists concurrently
                val detailDeferred = async { userService.getUserDetail(uid) }
                val levelDeferred = async { userService.getUserLevel() }
                val subcountDeferred = async { userService.getUserSubcount() }
                val playlistsDeferred = async { userService.getUserPlaylists(uid, limit = 1000) }

                val detail = detailDeferred.await()
                val level = levelDeferred.await()
                val subcount = subcountDeferred.await()
                val playlistsResponse = playlistsDeferred.await()
                
                val finalProfile = detail.profile ?: accountResponse.profile

                // Save Playlists
                val playlists = playlistsResponse.playlist ?: emptyList()
                profileStorage.savePlaylistsJson(gson.toJson(playlists))

                // Save to DataStore
                profileStorage.saveProfile(
                    CachedProfile(
                        nickname = finalProfile?.nickname,
                        avatarUrl = finalProfile?.avatarUrl,
                        backgroundUrl = finalProfile?.backgroundUrl,
                        level = level.data?.level,
                        listenSongs = detail.listenSongs,
                        followerCount = finalProfile?.followeds,
                        followingCount = finalProfile?.follows,
                        playlistCount = subcount.createdPlaylistCount
                    )
                )

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to fetch profile", e)
                _isLoading.value = false
                _isError.value = true
                _errorMessage.value = e.message ?: "网络异常"
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _currentUid.value = null
            cookieStorage.clearCookies("www.lightningrealm.cloud")
            cookieStorage.clearCookies("music.163.com")
            profileStorage.clearProfile()
        }
    }
}
