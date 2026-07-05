package com.lr.meow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.lr.animation.diysharedelement.component.CardAnimRoot
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.state.CardAnimState
import com.lr.glassui.captureBackground
import com.lr.meow.data.TopBarMenuItem
import com.lr.meow.data.navigation.EntryAlbumDetail
import com.lr.meow.data.navigation.EntryArtistDetail
import com.lr.meow.data.navigation.EntryDiscoverDetail
import com.lr.meow.data.navigation.EntryDiscoverRoot
import com.lr.meow.data.navigation.EntryHomeDetail
import com.lr.meow.data.navigation.EntryHomeRoot
import com.lr.meow.data.navigation.EntryLibraryRoot
import com.lr.meow.data.navigation.EntryPlaylistDetail
import com.lr.meow.data.navigation.EntryProfileRoot
import com.lr.meow.data.navigation.EntrySearchRoot
import com.lr.meow.data.navigation.MyNavTab
import com.lr.meow.feature.album.AlbumDetail
import com.lr.meow.feature.artist.ArtistDetail
import com.lr.meow.feature.discover.Discover
import com.lr.meow.feature.discover.DiscoverDetail
import com.lr.meow.feature.home.Home
import com.lr.meow.feature.home.HomeDetail
import com.lr.meow.feature.library.Library
import com.lr.meow.feature.login.Login
import com.lr.meow.feature.player.PlayerScreen
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.feature.playlist.PlaylistDetail
import com.lr.meow.feature.profile.Profile
import com.lr.meow.feature.profile.SharedUserIntent
import com.lr.meow.feature.profile.SharedUserViewModel
import com.lr.meow.feature.search.Search
import com.lr.meow.ui.common.component.glass.CustomFrostedGlassBottomBar
import com.lr.meow.ui.common.component.glass.FloatingTopBar
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.theme.LocalIsMusicPlaying
import com.lr.meow.ui.theme.LocalRootGraphicsLayer
import com.lr.meow.ui.theme.LocalTopBarMenuItems
import com.lr.meow.ui.theme.LocalTopBarPadding
import com.lr.meow.ui.theme.MeowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeowTheme {
                val backgroundLayer = rememberGraphicsLayer()
                val topBarMenuItems = remember{mutableListOf<TopBarMenuItem>()}
                CompositionLocalProvider(
                    LocalRootGraphicsLayer provides backgroundLayer,
                    LocalTopBarMenuItems provides topBarMenuItems
                ) {
                    RootView()
                }
            }
        }
    }
}

@Composable
fun RootView(
    viewModel: MainViewModel = koinViewModel(),
    sharedUserViewModel: SharedUserViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val density = LocalDensity.current
    val homeStack = rememberNavBackStack(EntryHomeRoot)
    val discoverStack = rememberNavBackStack(EntryDiscoverRoot)
    val libraryStack = rememberNavBackStack(EntryLibraryRoot)
    val searchStack = rememberNavBackStack(EntrySearchRoot)
    val profileStack = rememberNavBackStack(EntryProfileRoot)

    // 获取当前正在显示的栈
    val activeStack = when (uiState.currentTab) {
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
    var topBarHeight by remember { mutableStateOf(0.dp) }

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

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            sharedUserViewModel.dispatch(SharedUserIntent.RefreshProfile)
        }
    }

    val sourceCornerRadiusPx =
        with(LocalDensity.current) { 20.dp.toPx() } // 与 CardFace 的 RoundedCornerShape(20.dp) 一致
    val cardAnimScope = rememberCoroutineScope()
    val cardAnimState = remember {
        CardAnimState(sourceCornerRadiusPx, cardAnimScope)
    }

    var showPlayerScreen by remember { mutableStateOf(false) }

    val currentSong by playerViewModel.currentMediaItem.collectAsState()
    val isMusicPlayingState = currentSong != null

    // 在 isBottomBarVisible 旁边加：
    var isTopBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isBottomBarVisible, showPlayerScreen) {
        if (!isBottomBarVisible && !showPlayerScreen) {
            delay(250.milliseconds) // 等卡片过渡动画结束（约300ms）再渲染玻璃
            isTopBarVisible = true
        } else {
            isTopBarVisible = false  // 立即隐藏，不用等动画
        }
    }


    CompositionLocalProvider(
        LocalIsMusicPlaying provides isMusicPlayingState
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            val backgroundLayer = LocalRootGraphicsLayer.current!!

            CardAnimRoot(
                state = cardAnimState,
                modifier = Modifier.fillMaxSize()
            ) {
                CompositionLocalProvider(
                    LocalBottomBarPadding provides bottomBarHeight,
                    LocalTopBarPadding provides topBarHeight,
                    LocalIsLogin provides uiState.isLoggedIn,
                    LocalRequireAuth provides { viewModel.dispatch(MainIntent.RequestLogin) },
                    LocalCardAnimState provides cardAnimState
                ) {
                    NavDisplay(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .then(
                                if (isCaptureEnabled) Modifier.captureBackground(backgroundLayer) else Modifier
                            ),
                        backStack = activeStack,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using null
                        },
                        popTransitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using null
                        },
                        predictivePopTransitionSpec = { _ ->
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using null
                        },
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
                                HomeDetail(
                                    id = it.id,
                                    onBack = { activeStack.removeAt(activeStack.lastIndex) }
                                )
                            }

                            /**
                             * Discover对应栈
                             * **/
                            entry<EntryDiscoverRoot> {
                                Discover(
                                    onPlaylistClick = { playlistId, coverImgUrl ->
                                        discoverStack.add(EntryPlaylistDetail(playlistId, coverImgUrl))
                                    },
                                    onAlbumClick = { albumId, coverImgUrl ->
                                        discoverStack.add(EntryAlbumDetail(albumId, coverImgUrl))
                                    },
                                    onArtistClick = { artistId, coverImgUrl, artistName ->
                                        discoverStack.add(EntryArtistDetail(artistId, coverImgUrl, artistName))
                                    }
                                )
                            }

                            entry<EntryDiscoverDetail> {
                                DiscoverDetail()
                            }

                            /**
                             * Library对应栈
                             * **/
                            entry<EntryLibraryRoot> {
                                Library(
                                    viewModel = sharedUserViewModel,
                                    onPlaylistClick = { playlistId, coverImgUrl ->
                                        libraryStack.add(
                                            EntryPlaylistDetail(
                                                playlistId,
                                                coverImgUrl
                                            )
                                        )
                                    }
                                )
                            }

                            /**
                             * Search对应栈
                             * **/
                            entry<EntrySearchRoot> {
                                Search(
                                    onPlaylistClick = { playlistId, coverImgUrl ->
                                        searchStack.add(EntryPlaylistDetail(playlistId, coverImgUrl))
                                    },
                                    onAlbumClick = { albumId, coverImgUrl ->
                                        searchStack.add(EntryAlbumDetail(albumId, coverImgUrl))
                                    },
                                    onArtistClick = { artistId, coverImgUrl, artistName ->
                                        searchStack.add(EntryArtistDetail(artistId, coverImgUrl, artistName))
                                    }
                                )
                            }

                            entry<EntryProfileRoot> {
                                Profile(
                                    onPlaylistClick = { playlistId, coverImgUrl ->
                                        profileStack.add(
                                            EntryPlaylistDetail(
                                                playlistId,
                                                coverImgUrl
                                            )
                                        )
                                    }
                                )
                            }

                            /**
                             * Playlist Detail (Can be in any stack)
                             * **/
                            entry<EntryPlaylistDetail> {
                                PlaylistDetail(
                                    playlistId = it.id,
                                    coverImgUrl = it.coverImgUrl,
                                    onBack = { activeStack.removeAt(activeStack.lastIndex) },
                                    onAlbumClick = { albumId, coverImgUrl ->
                                        activeStack.add(EntryAlbumDetail(albumId, coverImgUrl))
                                    }
                                )
                            }

                            /**
                             * Album Detail (Can be in any stack)
                             * **/
                            entry<EntryAlbumDetail> {
                                AlbumDetail(
                                    albumId = it.id,
                                    coverImgUrl = it.coverImgUrl,
                                    onBack = { activeStack.removeAt(activeStack.lastIndex) }
                                )
                            }
                            
                            /**
                             * Artist Detail (Can be in any stack)
                             * **/
                            entry<EntryArtistDetail> {
                                ArtistDetail(
                                    artistId = it.id,
                                    artistName = it.name ?: "",
                                    artistAvatarUrl = it.coverImgUrl ?: "",
                                    onBack = { activeStack.removeAt(activeStack.lastIndex) }
                                )
                            }
                        }
                    )
                }

            } // End of CardAnimRoot

            val isMusicPlaying = LocalIsMusicPlaying.current

            AnimatedVisibility(
                visible = isBottomBarVisible || isMusicPlaying,
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
                ) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CustomFrostedGlassBottomBar(
                    modifier = Modifier
                        .onLayoutRectChanged{
                            if(!showPlayerScreen){
                                bottomBarHeight = with(density){
                                    it.height.toDp()+16.dp
                                }
                            }
                        },
                    currentTab = uiState.currentTab,
                    isBottomBarVisible = isBottomBarVisible,
                    isExpanded = showPlayerScreen,
                    graphicsLayer = backgroundLayer,
                    onMiniPlayerClick = {
                        showPlayerScreen = true
                    },
                    onTabSelected = { selectedTab ->
                        viewModel.dispatch(MainIntent.ChangeTab(selectedTab))
                    },
                    playerScreenContent = { glassEnv ->
                        PlayerScreen(
                            glassEnv = glassEnv,
                            onBack = { showPlayerScreen = false }
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                FloatingTopBar(
                    graphicsLayer = backgroundLayer,
                    modifier = Modifier
                        .onLayoutRectChanged{
                            topBarHeight = with(density){
                                it.height.toDp()+8.dp
                            }
                        }
                ) {
                    cardAnimState.prepareCollapse()
                    activeStack.removeAt(activeStack.lastIndex)
                    cardAnimState.animScope.launch { cardAnimState.runCollapse() }
                }
            }

            Login(
                showBottomSheet = uiState.showLoginSheet,
                onDismissRequest = { viewModel.dispatch(MainIntent.DismissLogin) }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RootPreview() {
    Home { }
}