package com.lr.meow.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.meow.LocalIsLogin
import com.lr.meow.LocalRequireAuth
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.components.shimmerEffect
import com.lr.meow.ui.theme.LocalSharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import org.koin.androidx.compose.koinViewModel

@Composable
fun Discover(
    viewModel: DiscoverViewModel = koinViewModel(),
    onPlaylistClick: (Long, String?) -> Unit = { _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLoggedIn = LocalIsLogin.current
    val requireAuth = LocalRequireAuth.current
    val uiState by viewModel.uiState.collectAsState()

    val sharedScope = LocalSharedTransitionScope.current ?: return
    val animatedScope = LocalNavAnimatedContentScope.current

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && uiState.recommendPlaylists.isEmpty() && !uiState.isError) {
            viewModel.fetchRecommendations()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        if (!isLoggedIn || uiState.needsLogin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.needsLogin) "登录已过期，请重新登录" else "登录后发现更多好音乐",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(onClick = { requireAuth() }) {
                    Text(if (uiState.needsLogin) "重新登录" else "立即登录")
                }
            }
        } else if (uiState.isLoading && uiState.recommendPlaylists.isEmpty()) {
            DiscoverSkeleton(colorScheme)
        } else if (uiState.isError && uiState.recommendPlaylists.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorMessage ?: "加载失败",
                    color = colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { viewModel.fetchRecommendations() }) {
                    Text("重试")
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(Modifier.statusBarsPadding())
                    Text(
                        text = "发现",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                if (uiState.recommendPlaylists.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "推荐歌单",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.recommendPlaylists) { playlist ->
                                    Column(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .bouncyClickable { onPlaylistClick(playlist.id, playlist.picUrl) }
                                    ) {
                                        with(sharedScope) {
                                            AsyncImage(
                                                model = playlist.picUrl,
                                                contentDescription = playlist.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(colorScheme.surfaceVariant)
                                                    .sharedBounds(
                                                        sharedContentState = rememberSharedContentState(key = "playlist_cover_${playlist.id}"),
                                                        animatedVisibilityScope = animatedScope,
                                                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp))
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = playlist.name,
                                            color = colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            lineHeight = 18.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.toplists.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "官方榜单",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.toplists) { toplist ->
                                    Column(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .bouncyClickable { onPlaylistClick(toplist.id, toplist.coverImgUrl) }
                                    ) {
                                        with(sharedScope) {
                                            AsyncImage(
                                                model = toplist.coverImgUrl,
                                                contentDescription = toplist.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .sharedElement(
                                                        sharedContentState = rememberSharedContentState(key = "playlist_cover_${toplist.id}"),
                                                        animatedVisibilityScope = animatedScope
                                                    )
                                                    .width(140.dp)
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(colorScheme.surfaceVariant)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = toplist.name,
                                            color = colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            lineHeight = 18.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.newAlbums.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "新碟首发",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.newAlbums) { album ->
                                    Column(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .bouncyClickable { /* TODO: Open Album */ }
                                    ) {
                                        AsyncImage(
                                            model = album.picUrl,
                                            contentDescription = album.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(colorScheme.surfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = album.name,
                                            color = colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        album.artist?.let { artist ->
                                            Text(
                                                text = artist.name,
                                                color = colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.topArtists.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "热门歌手",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.topArtists) { artist ->
                                    Column(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .bouncyClickable { /* TODO: Open Artist */ },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AsyncImage(
                                            model = artist.picUrl,
                                            contentDescription = artist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(120.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(colorScheme.surfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = artist.name,
                                            color = colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }



                item {
                    Spacer(Modifier.height(LocalBottomBarPadding.current))
                }
            }
        }
    }
}

@Composable
private fun DiscoverSkeleton(colorScheme: androidx.compose.material3.ColorScheme) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(Modifier.statusBarsPadding())
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .width(100.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }

        // Section Skeleton Helper
        val sectionSkeleton: @Composable (isCircle: Boolean, hasSubtitle: Boolean) -> Unit = { isCircle, hasSubtitle ->
            Column {
                Box(
                    Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                        .width(100.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(4) {
                        Column(horizontalAlignment = if (isCircle) Alignment.CenterHorizontally else Alignment.Start) {
                            val imgModifier = if (isCircle) {
                                Modifier.width(120.dp).height(120.dp).clip(androidx.compose.foundation.shape.CircleShape)
                            } else {
                                Modifier.width(140.dp).height(140.dp).clip(RoundedCornerShape(16.dp))
                            }
                            Box(imgModifier.shimmerEffect())
                            Spacer(Modifier.height(10.dp))
                            Box(
                                Modifier
                                    .width(80.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .shimmerEffect()
                            )
                            if (hasSubtitle) {
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .width(60.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 推荐歌单
        item { sectionSkeleton(false, true) }
        
        // 官方榜单
        item { sectionSkeleton(false, false) }
        
        // 新碟首发
        item { sectionSkeleton(false, true) }
        
        // 热门歌手
        item { sectionSkeleton(true, false) }
    }
}