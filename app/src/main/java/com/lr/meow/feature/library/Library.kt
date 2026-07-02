package com.lr.meow.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.meow.LocalIsLogin
import com.lr.meow.LocalRequireAuth
import com.lr.meow.feature.profile.SharedUserViewModel
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.lr.meow.ui.theme.LocalSharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.spring
import com.lr.animation.diysharedelement.model.CardAnimTransform
import kotlinx.coroutines.launch

@Composable
fun Library(
    viewModel: SharedUserViewModel = koinViewModel(),
    onPlaylistClick: (Long, String?) -> Unit = { _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLoggedIn = LocalIsLogin.current
    val requireAuth = LocalRequireAuth.current
    
    val uiState by viewModel.uiState.collectAsState()

    val cardAnimState = com.lr.animation.diysharedelement.component.LocalCardAnimState.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val targetSizePx = with(density) { 120.dp.toPx() }
    val targetXPx = with(density) { 20.dp.toPx() }
    val targetYPx = with(density) { 140.dp.toPx() }
    val targetRadiusPx = with(density) { 16.dp.toPx() }

    val playlists by viewModel.playlistsFlow.collectAsState()
    val currentUid by viewModel.currentUid.collectAsState()

    val createdPlaylists = remember(playlists, currentUid) {
        playlists.filter { it.creator?.userId == currentUid }
    }
    val subscribedPlaylists = remember(playlists, currentUid) {
        playlists.filter { it.creator?.userId != currentUid }
    }

    // LaunchedEffect removed because data fetch is now handled globally in MainActivity

    val tabs = listOf("我的音乐", "收藏歌单")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        if (!isLoggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "登录后查看您的音乐库",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(onClick = { requireAuth() }) {
                    Text("立即登录")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.statusBarsPadding())
                
                Text(
                    text = "音乐库",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                    divider = { },
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = colorScheme.primary
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium
                                ) 
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val displayList = if (page == 0) createdPlaylists else subscribedPlaylists
                    
                    if (displayList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = colorScheme.primary)
                            } else {
                                Text(
                                    text = "暂无歌单",
                                    color = colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 16.dp, bottom = LocalBottomBarPadding.current),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = displayList,
                                key = { "playlist_${it.id}" },
                                contentType = { "playlist" }
                            ) { playlist ->
                                Row(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .bouncyClickable { 
                                            val ready = cardAnimState.prepareExpand("playlist_cover_${playlist.id}") { _ ->
                                                CardAnimTransform(
                                                    x = targetXPx,
                                                    y = targetYPx,
                                                    width = targetSizePx,
                                                    height = targetSizePx,
                                                    cornerRadius = targetRadiusPx
                                                )
                                            }
                                            if (ready) {
                                                onPlaylistClick(playlist.id, playlist.coverImgUrl)
                                                cardAnimState.animScope.launch { cardAnimState.runExpand() }
                                            }
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.lr.animation.diysharedelement.modifier.SharedElement(
                                        cardId = "playlist_cover_${playlist.id}",
                                        modifier = Modifier
                                            .size(64.dp)
                                    ) {
                                        AsyncImage(
                                            model = playlist.coverImgUrl,
                                            contentDescription = "Playlist Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colorScheme.surfaceVariant)
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = playlist.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colorScheme.onBackground,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "${playlist.trackCount} 首",
                                            fontSize = 14.sp,
                                            color = colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}