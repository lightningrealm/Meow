package com.lr.meow.ui.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.dp

val LocalBottomBarPadding = compositionLocalOf {
    0.dp
}

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalRootGraphicsLayer = compositionLocalOf<GraphicsLayer?>{ null }
val LocalIsMusicPlaying = compositionLocalOf { false }