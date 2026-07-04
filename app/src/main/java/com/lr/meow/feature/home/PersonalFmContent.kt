package com.lr.meow.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.lr.meow.R
import com.lr.meow.feature.player.PlaybackMode
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.common.card.SwipeableTinderCard
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding

import org.koin.androidx.compose.koinViewModel
import com.lr.meow.feature.profile.SharedUserViewModel
import com.lr.meow.ui.theme.LocalTopBarPadding

@Composable
fun PersonalFmContent(
    viewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    sharedUserViewModel: SharedUserViewModel = koinViewModel(),
    uiState: HomeUiState,
    isLoggedIn: Boolean,
    requireAuth: () -> Unit,
    colorScheme: ColorScheme
) {
    val fmQueue by playerViewModel.fmQueue.collectAsState()
    val playbackMode by playerViewModel.playbackMode.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val isFetchingFm by playerViewModel.isFetchingFm.collectAsState()
    val likedSongIds by sharedUserViewModel.likedSongIds.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        playerViewModel.loadInitialFmSongs()
    }

    if (!isLoggedIn || uiState.needsLogin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (uiState.needsLogin) stringResource(id = R.string.login_expired) else stringResource(
                    id = R.string.login_listen_fm
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { requireAuth() }) {
                Text(
                    if (uiState.needsLogin) stringResource(id = R.string.relogin) else stringResource(
                        id = R.string.login_now
                    )
                )
            }
        }
    } else if (isFetchingFm && fmQueue.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                Modifier
                    .padding(LocalTopBarPadding.current)
            )
            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 24.dp,
                    top = 16.dp
                )
            ) {
                Text(
                    text = "PERSONAL FM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                Text(
                    text = stringResource(id = R.string.private_fm),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (playbackMode == PlaybackMode.FM && fmQueue.isEmpty()) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    val displayList = fmQueue
                    // To display top 3 cards, we reverse so index 0 is on top
                    val displaySongs = displayList.take(3).reversed()

                    displaySongs.forEachIndexed { index, song ->
                        key(song.id) {
                            // index 0 here is the bottom-most displayed, index 2 is top
                            val targetScale = 1f - (displaySongs.size - 1 - index) * 0.05f
                            val targetYOffsetBase = (displaySongs.size - 1 - index) * 16f

                            val animatedScale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "cardScale"
                            )

                            val animatedYOffsetBase by animateFloatAsState(
                                targetValue = targetYOffsetBase,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "cardYOffset"
                            )

                            val enterAnim = remember(song.id) { Animatable(1000f) }
                            LaunchedEffect(song.id) {
                                enterAnim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }

                            SwipeableTinderCard(
                                enabled = playbackMode == PlaybackMode.FM && !isFetchingFm,
                                onSwiped = { direction ->
                                    if (playbackMode == PlaybackMode.FM) {
                                        playerViewModel.skipToNext()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .offset {
                                        IntOffset(
                                            x = 0,
                                            y = (animatedYOffsetBase + enterAnim.value).dp.roundToPx()
                                        )
                                    }
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    }
                                    .zIndex(index.toFloat())
                            ) {
                                // Card Content
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(16.dp, RoundedCornerShape(24.dp))
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(colorScheme.surface)
                                ) {
                                    AsyncImage(
                                        model = song.al?.picUrl,
                                        contentDescription = "Song Cover",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colorScheme.surfaceVariant)
                                    )

                                    // Bottom gradient for text readability
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.5f)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.8f)
                                                    )
                                                )
                                            )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(24.dp)
                                    ) {
                                        Text(
                                            text = song.name
                                                ?: stringResource(id = R.string.unknown_song),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = song.artistName,
                                            fontSize = 18.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        } // End of key(song.id)
                    }
                } // End of else block
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 48.dp)
                    .padding(bottom = LocalBottomBarPadding.current),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClickable {
                            if (playbackMode == PlaybackMode.FM) playerViewModel.skipToNext()
                        }
                        .clip(RoundedCornerShape(32.dp))
                        .background(colorScheme.surface.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Skip",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .bouncyClickable {
                            if (playbackMode == PlaybackMode.FM) {
                                if (isPlaying) playerViewModel.pause() else playerViewModel.play()
                            } else {
                                playerViewModel.playFm()
                            }
                        }
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackMode == PlaybackMode.FM && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Like Button
                val topSong = fmQueue.firstOrNull()
                val isLiked = topSong?.let { likedSongIds.contains(it.id) } ?: false
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClickable {
                            topSong?.let { sharedUserViewModel.toggleLike(it.id) }
                        }
                        .clip(RoundedCornerShape(32.dp))
                        .background(colorScheme.surface.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isLiked,
                        transitionSpec = {
                            scaleIn(tween(200)) togetherWith scaleOut(tween(200))
                        },
                        label = "likeAnim"
                    ) { liked ->
                        Icon(
                            imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) Color(0xFFFF512F) else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
