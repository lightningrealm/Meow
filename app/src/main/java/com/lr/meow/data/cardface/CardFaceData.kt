package com.lr.meow.data.cardface

import androidx.compose.ui.graphics.Color

data class CardFaceData(
    val topTitle: String,
    val title: String,
    val subTitle : String,
    val backgroundGradient: List<Color> = listOf(
        Color(0xFF0D0221), Color(0xFF1B2F69), Color(
            0xFF3A3C9E
        )
    ),

)
