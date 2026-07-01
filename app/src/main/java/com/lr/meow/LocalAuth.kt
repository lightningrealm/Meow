package com.lr.meow

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 全局登录状态。可以通过 LocalIsLogin.current 随时获取是否已登录。
 */
val LocalIsLogin = staticCompositionLocalOf { false }

/**
 * 全局一键呼出登录面板的回调。
 * 深层级组件可通过 LocalRequireAuth.current() 来唤起登录界面。
 */
val LocalRequireAuth = staticCompositionLocalOf<() -> Unit> {
    { error("LocalRequireAuth is not provided") }
}
