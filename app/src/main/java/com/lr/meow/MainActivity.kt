package com.lr.meow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.lr.glassui.captureBackground
import com.lr.meow.data.navigation.EntryDiscoverDetail
import com.lr.meow.data.navigation.EntryDiscoverRoot
import com.lr.meow.data.navigation.EntryHomeDetail
import com.lr.meow.data.navigation.EntryHomeRoot
import com.lr.meow.data.navigation.EntryLibraryRoot
import com.lr.meow.data.navigation.EntryProfileRoot
import com.lr.meow.data.navigation.EntrySearchRoot
import com.lr.meow.data.navigation.MyNavTab
import com.lr.meow.feature.discover.Discover
import com.lr.meow.feature.discover.DiscoverDetail
import com.lr.meow.feature.home.Home
import com.lr.meow.feature.home.HomeDetail
import com.lr.meow.feature.library.Library
import com.lr.meow.feature.profile.Profile
import com.lr.meow.feature.search.Search
import com.lr.meow.ui.common.component.glass.CustomFrostedGlassBottomBar
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.theme.LocalRootGraphicsLayer
import com.lr.meow.ui.theme.LocalSharedTransitionScope
import com.lr.meow.ui.theme.MeowTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeowTheme {
                val backgroundLayer = rememberGraphicsLayer()
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this,
                        LocalRootGraphicsLayer provides backgroundLayer
                    ) {
                        RootView()
                    }
                }
            }
        }
    }
}

@Composable
fun RootView() {
    val density = LocalDensity.current
    val homeStack = rememberNavBackStack(EntryHomeRoot)
    val discoverStack = rememberNavBackStack(EntryDiscoverRoot)
    val libraryStack = rememberNavBackStack(EntryLibraryRoot)
    val searchStack = rememberNavBackStack(EntrySearchRoot)
    val profileStack = rememberNavBackStack(EntryProfileRoot)

    var currentTab by remember { mutableStateOf(MyNavTab.HOME) }

    // 获取当前正在显示的栈
    val activeStack = when (currentTab) {
        MyNavTab.HOME -> homeStack
        MyNavTab.DISCOVER -> discoverStack
        MyNavTab.LIBRARY -> libraryStack
        MyNavTab.SEARCH -> searchStack
        MyNavTab.PROFILE -> profileStack
    }

    // Navigation 3 中，返回栈就是一个普通 List。
    // 如果当前栈里只有一个元素，说明我们在底栏的根页面，显示底栏。
    // 如果大于 1，说明进到了深层页面，隐藏底栏。
    val isBottomBarVisible = activeStack.size == 1
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    
    // 平滑性能优化：延迟关闭截图。等底栏的 fadeOut 退出动画播完后，再关闭采集，彻底杜绝“突兀感”
    var isCaptureEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(isBottomBarVisible) {
        if (isBottomBarVisible) {
            isCaptureEnabled = true
        } else {
            delay(300.milliseconds) // 等待底栏 250ms 的退出动画以及 sharedBounds 播完
            isCaptureEnabled = false
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        val backgroundLayer = LocalRootGraphicsLayer.current!!
        CompositionLocalProvider(
            LocalBottomBarPadding provides bottomBarHeight
        ) {
            NavDisplay(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .then(if (isCaptureEnabled) Modifier.captureBackground(backgroundLayer) else Modifier),
                backStack = activeStack,
                entryProvider = entryProvider {
                    /**
                     * Home对应栈
                     * **/
                    entry<EntryHomeRoot> {
                        Home { clickedId ->
                            homeStack.add(EntryHomeDetail(clickedId))
                        }
                    }
                    entry<EntryHomeDetail> {
                        HomeDetail(it.id)
                    }

                    /**
                     * Discover对应栈
                     * **/
                    entry<EntryDiscoverRoot> {
                        Discover()
                    }

                    entry<EntryDiscoverDetail> {
                        DiscoverDetail()
                    }

                    /**
                     * Library对应栈
                     * **/
                    entry<EntryLibraryRoot> {
                        Library()
                    }

                    /**
                     * Search对应栈
                     * **/
                    entry<EntrySearchRoot> {
                        Search()
                    }

                    entry<EntryProfileRoot>{
                        Profile()
                    }
                }
            )
        }
        val sharedTransitionScope = LocalSharedTransitionScope.current!!
        with(sharedTransitionScope) {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut()
            ) {
                CustomFrostedGlassBottomBar(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            bottomBarHeight = with(density) {
                                coordinates.size.height.toDp() + 16.dp
                            }
                        },
                    currentTab = currentTab,
                    graphicsLayer = backgroundLayer,
                ) { selectedTab ->
                    currentTab = selectedTab
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RootPreview() {
    Home { }
}