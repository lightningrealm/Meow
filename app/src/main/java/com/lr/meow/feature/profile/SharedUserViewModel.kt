package com.lr.meow.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lr.core.datastore.CachedProfile
import com.lr.core.datastore.ProfileStorage
import com.lr.core.network.PersistentCookieJar
import com.lr.core.network.api.MeowAuthService
import com.lr.core.network.api.MeowUserService
import com.lr.core.network.model.Playlist
import com.lr.core.network.model.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.lr.meow.ui.common.util.UiText
import com.lr.meow.R



class SharedUserViewModel(
    private val userService: MeowUserService,
    private val authService: MeowAuthService,
    private val persistentCookieJar: PersistentCookieJar,
    private val profileStorage: ProfileStorage
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<UiText?>(null)

    // Combine loading states with cached profile flow
    val uiState: StateFlow<SharedUserUiState> = combine(
        _isLoading,
        _isError,
        _errorMessage,
        profileStorage.profileFlow
    ) { isLoading, isError, errorMessage, cachedProfile ->
        SharedUserUiState(
            isLoading = isLoading,
            isError = isError,
            errorMessage = errorMessage,
            userProfile = if (cachedProfile.nickname != null) {
                UserProfile(
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
        initialValue = SharedUserUiState()
    )

    val currentUid: StateFlow<Long?> = profileStorage.profileFlow.map { it.userId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentSongsPagingFlow = currentUid.flatMapLatest { uid ->
        if (uid == null) {
            kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(pageSize = 30),
                pagingSourceFactory = { RecentSongPagingSource(userService) }
            ).flow
        }
    }.cachedIn(viewModelScope)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentPlaylistsPagingFlow = currentUid.flatMapLatest { uid ->
        if (uid == null) {
            kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(pageSize = 30),
                pagingSourceFactory = { RecentPlaylistPagingSource(userService) }
            ).flow
        }
    }.cachedIn(viewModelScope)

    private val gson = Gson()

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

    fun dispatch(intent: SharedUserIntent) {
        when (intent) {
            is SharedUserIntent.RefreshProfile -> fetchProfileData()
        }
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            try {
                // Check login status first
                val statusResponse = authService.checkLoginStatus()
                val accountId = statusResponse.data?.account?.id
                if (accountId == null) {
                    _isLoading.value = false
                    _isError.value = true
                    _errorMessage.value = UiText.StringResource(R.string.login_expired)
                    logout()
                    return@launch
                }
                
                // Refresh login asynchronously in background
                launch {
                    try { authService.refreshLogin() } catch (e: Exception) { /* ignore */ }
                }

                // To fetch details, we first need to know the UID.
                // The /user/account endpoint tells us the currently logged-in user's profile and ID.
                val accountResponse = userService.getUserAccount()
                val uid = accountResponse.profile?.userId ?: accountResponse.account?.id

                if (uid == null) {
                    _isLoading.value = false
                    _isError.value = true
                    _errorMessage.value = UiText.StringResource(R.string.unknown_user_id)
                    logout()
                    return@launch
                }
                
                // Removed _currentUid.value = uid

                // Now we can fetch details, level, subcount and playlists concurrently
                val detailDeferred = async { try { userService.getUserDetail(uid) } catch (e: Exception) { null } }
                val levelDeferred = async { try { userService.getUserLevel() } catch (e: Exception) { null } }
                val subcountDeferred = async { try { userService.getUserSubcount() } catch (e: Exception) { null } }
                val playlistsDeferred = async { try { userService.getUserPlaylists(uid, limit = 1000) } catch (e: Exception) { null } }

                val detail = detailDeferred.await()
                val level = levelDeferred.await()
                val subcount = subcountDeferred.await()
                val playlistsResponse = playlistsDeferred.await()
                
                val finalProfile = detail?.profile ?: accountResponse.profile

                // Save Playlists
                val playlists = playlistsResponse?.playlist ?: emptyList()
                profileStorage.savePlaylistsJson(gson.toJson(playlists))

                // Save to DataStore
                profileStorage.saveProfile(
                    CachedProfile(
                        userId = uid,
                        nickname = finalProfile?.nickname ?: accountResponse.account?.userName,
                        avatarUrl = finalProfile?.avatarUrl,
                        backgroundUrl = finalProfile?.backgroundUrl,
                        level = level?.data?.level,
                        listenSongs = detail?.listenSongs,
                        followerCount = finalProfile?.followeds,
                        followingCount = finalProfile?.follows,
                        playlistCount = subcount?.createdPlaylistCount
                    )
                )

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("SharedUserViewModel", "Failed to fetch profile", e)
                _isLoading.value = false
                _isError.value = true
                _errorMessage.value = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.network_error)
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            persistentCookieJar.clearCookies("112.124.4.51")
            persistentCookieJar.clearCookies("music.163.com")
            profileStorage.clearProfile()
        }
    }
}
