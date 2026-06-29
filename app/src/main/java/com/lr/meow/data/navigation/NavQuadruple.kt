package com.lr.meow.data.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavQuadruple(
    val destGraph: MyNavTab,
    val outlinedIcon: ImageVector,
    val icon: ImageVector,
    val label: String
)