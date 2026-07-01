package com.lr.meow.feature.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.lr.meow.LocalIsLogin
import com.lr.meow.LocalRequireAuth
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.components.bouncyClickable
import org.koin.androidx.compose.koinViewModel
import com.lr.meow.ui.theme.LocalSharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope

@Composable
fun Profile(
    viewModel: SharedUserViewModel = koinViewModel(),
    onPlaylistClick: (Long, String?) -> Unit = { _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLoggedIn = LocalIsLogin.current
    val requireAuth = LocalRequireAuth.current
    val uiState by viewModel.uiState.collectAsState()

    val recentSongs = viewModel.recentSongsPagingFlow.collectAsLazyPagingItems()
    val recentPlaylists = viewModel.recentPlaylistsPagingFlow.collectAsLazyPagingItems()

    val sharedScope = LocalSharedTransitionScope.current ?: return
    val animatedScope = LocalNavAnimatedContentScope.current

    var isSongsExpanded by remember { mutableStateOf(true) }
    var isPlaylistsExpanded by remember { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        if (isLoggedIn && uiState.userProfile?.backgroundUrl != null) {
            AsyncImage(
                model = uiState.userProfile?.backgroundUrl,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient overlay to ensure text is readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.background.copy(alpha = 0.3f),
                                colorScheme.background.copy(alpha = 0.8f),
                                colorScheme.background
                            )
                        )
                    )
            )
        }

        if (!isLoggedIn) {
            // Unauthenticated UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Discover More Music",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Login to access your profile and library",
                    fontSize = 14.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { requireAuth() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(horizontal = 32.dp).height(48.dp)
                ) {
                    Text("Login Now", fontWeight = FontWeight.Bold)
                }
            }
        } else if (uiState.isLoading && uiState.userProfile == null) {
            // Loading UI (only when no cached data is available)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            // Authenticated Profile UI
            LazyColumn(
                contentPadding = PaddingValues(top = 20.dp, bottom = LocalBottomBarPadding.current),
                modifier = Modifier.fillMaxSize()
            ) {
                // Profile Header
                item {
                    Spacer(Modifier.statusBarsPadding())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colorScheme.onBackground
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        if (uiState.userProfile?.avatarUrl != null) {
                            AsyncImage(
                                model = uiState.userProfile?.avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary.copy(alpha = 0.2f))
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = uiState.userProfile?.nickname ?: "Guest",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.followerCount ?: 0} Followers",
                                fontSize = 14.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${uiState.followingCount ?: 0} Following",
                                fontSize = 14.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Lv.${uiState.userLevel ?: 0}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.onBackground,
                                contentColor = colorScheme.background
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(horizontal = 32.dp).height(48.dp)
                        ) {
                            Text("Logout", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
                
                // Playlists section moved to Library Tab

                // Recent Songs Section
                if (recentSongs.itemCount > 0) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSongsExpanded = !isSongsExpanded }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "最近播放 - 歌曲",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Icon(
                                imageVector = if (isSongsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse",
                                tint = colorScheme.onBackground
                            )
                        }
                    }
                    item {
                        AnimatedVisibility(
                            visible = isSongsExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                items(count = recentSongs.itemCount) { index ->
                                    recentSongs[index]?.let { recentItem ->
                                        val song = recentItem.song
                                        Column(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .bouncyClickable { /* TODO: Play song */ }
                                        ) {
                                            AsyncImage(
                                                model = song.al?.picUrl,
                                                contentDescription = "Song Cover",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(colorScheme.surfaceVariant)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = song.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onBackground,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = song.artistName,
                                                fontSize = 12.sp,
                                                color = colorScheme.onBackground.copy(alpha = 0.6f),
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

                // Recent Playlists Section
                if (recentPlaylists.itemCount > 0) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPlaylistsExpanded = !isPlaylistsExpanded }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "最近播放 - 歌单",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Icon(
                                imageVector = if (isPlaylistsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse",
                                tint = colorScheme.onBackground
                            )
                        }
                    }
                    item {
                        AnimatedVisibility(
                            visible = isPlaylistsExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                items(count = recentPlaylists.itemCount) { index ->
                                    recentPlaylists[index]?.let { recentItem ->
                                        val playlist = recentItem.playlist
                                        Column(
                                            modifier = Modifier
                                                .width(120.dp)
                                                .bouncyClickable { onPlaylistClick(playlist.id, playlist.coverImgUrl) }
                                        ) {
                                            with(sharedScope) {
                                                AsyncImage(
                                                    model = playlist.coverImgUrl,
                                                    contentDescription = "Playlist Cover",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(120.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(colorScheme.surfaceVariant)
                                                        .sharedBounds(
                                                            sharedContentState = rememberSharedContentState(key = "playlist_cover_${playlist.id}"),
                                                            animatedVisibilityScope = animatedScope,
                                                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp))
                                                        )
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = playlist.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onBackground,
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
            }
        }
    }
}