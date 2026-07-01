package com.lr.meow

import com.lr.meow.data.navigation.MyNavTab

/**
 * MVI Contract for MainActivity
 */

data class MainUiState(
    val currentTab: MyNavTab = MyNavTab.HOME,
    val showLoginSheet: Boolean = false,
    val isLoggedIn: Boolean = false
)

sealed interface MainIntent {
    data class ChangeTab(val tab: MyNavTab) : MainIntent
    object RequestLogin : MainIntent
    object DismissLogin : MainIntent
}
