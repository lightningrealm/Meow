package com.lr.meow.data.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class NavQuadruple(
    val destGraph: MyNavTab,
    val outlinedIcon: ImageVector,
    val icon: ImageVector,
    val label: String
)
val myNavTabs = listOf(
    NavQuadruple(
        MyNavTab.HOME,
        Icons.Outlined.Home,
        Icons.Default.Home,
        "主页"
    ),
    NavQuadruple(
        MyNavTab.DISCOVER,
        Icons.Outlined.Explore,
        Icons.Default.Explore,
        "发现"
    ),
    NavQuadruple(
        MyNavTab.LIBRARY,
        Icons.Outlined.LibraryMusic,
        Icons.Default.LibraryMusic,
        "音乐库"
    ),
    NavQuadruple(
        MyNavTab.SEARCH,
        Icons.Outlined.Search,
        Icons.Default.Search,
        "搜索"
    ),
    NavQuadruple(
        MyNavTab.PROFILE,
        Icons.Outlined.Person,
        Icons.Default.Person,
        "档案"
    )
)