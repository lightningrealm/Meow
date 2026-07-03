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
import com.lr.meow.R

data class NavQuadruple(
    val destGraph: MyNavTab,
    val outlinedIcon: ImageVector,
    val icon: ImageVector,
    val labelResId: Int
)
val myNavTabs = listOf(
    NavQuadruple(
        MyNavTab.HOME,
        Icons.Outlined.Home,
        Icons.Default.Home,
        R.string.nav_home
    ),
    NavQuadruple(
        MyNavTab.DISCOVER,
        Icons.Outlined.Explore,
        Icons.Default.Explore,
        R.string.nav_discover
    ),
    NavQuadruple(
        MyNavTab.LIBRARY,
        Icons.Outlined.LibraryMusic,
        Icons.Default.LibraryMusic,
        R.string.nav_library
    ),
    NavQuadruple(
        MyNavTab.SEARCH,
        Icons.Outlined.Search,
        Icons.Default.Search,
        R.string.nav_search
    ),
    NavQuadruple(
        MyNavTab.PROFILE,
        Icons.Outlined.Person,
        Icons.Default.Person,
        R.string.nav_profile
    )
)