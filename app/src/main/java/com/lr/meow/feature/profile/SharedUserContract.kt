package com.lr.meow.feature.profile

import com.lr.core.network.model.UserDetailResponse
import com.lr.core.network.model.UserLevelResponse
import com.lr.core.network.model.UserProfile
import com.lr.core.network.model.UserSubcountResponse

import com.lr.meow.ui.common.util.UiText

/**
 * MVI Contract for Profile Screen
 */
data class SharedUserUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: UiText? = null,
    
    // User Info
    val userProfile: UserProfile? = null,
    val userLevel: Int? = null,
    val listenSongs: Int? = null,
    
    // Counts
    val playlistCount: Int? = null,
    val followerCount: Int? = null,
    val followingCount: Int? = null
)

sealed class SharedUserIntent {
    object RefreshProfile : SharedUserIntent()
}
