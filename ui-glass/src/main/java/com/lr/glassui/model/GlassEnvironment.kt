package com.lr.glassui.model

import androidx.compose.ui.graphics.Color


data class GlassEnvironment(
    val dominantColor: Color,
    val luminance: Double,
    val isDark: Boolean
)
