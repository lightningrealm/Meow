package com.lr.meow.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.dp
import com.lr.meow.data.TopBarMenuItem

val LocalBottomBarPadding = compositionLocalOf {
    0.dp
}

val LocalTopBarPadding = compositionLocalOf {
    0.dp
}
val LocalTopBarMenuItems = compositionLocalOf {
    mutableListOf<TopBarMenuItem>()
}
val LocalRootGraphicsLayer = compositionLocalOf<GraphicsLayer?>{ null }
val LocalIsMusicPlaying = compositionLocalOf { false }