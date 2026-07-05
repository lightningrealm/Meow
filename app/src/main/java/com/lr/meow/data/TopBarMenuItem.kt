package com.lr.meow.data

import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
