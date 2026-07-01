package com.lr.meow.feature.profile

import com.lr.core.network.model.UserDetailResponse
import com.lr.core.network.model.UserLevelResponse
import com.lr.core.network.model.UserProfile
import com.lr.core.network.model.UserSubcountResponse

/**
 * MVI Contract for Profile Screen
 */
data class SharedUserUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    
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
