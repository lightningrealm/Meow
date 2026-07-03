package com.lr.meow.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.meow.LocalIsLogin
import com.lr.meow.LocalRequireAuth
import com.lr.meow.R
import com.lr.meow.ui.common.card.SwipeDirection
import com.lr.meow.ui.common.card.SwipeableTinderCard
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.components.shimmerEffect
import com.lr.meow.ui.theme.LocalBottomBarPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.lr.meow.feature.player.PlayerViewModel

@Composable
fun HomeDetail(
    id: Int,
    viewModel: HomeViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val cardAnimState = LocalCardAnimState.current
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn = LocalIsLogin.current
    val requireAuth = LocalRequireAuth.current

    LaunchedEffect(isLoggedIn, id) {
        if (id == 0 && isLoggedIn && uiState.recommendSongs.isEmpty() && !uiState.isError) {
            viewModel.fetchRecommendSongs()
        }
        if (id == 1 && isLoggedIn && uiState.personalFmSongs.isEmpty() && !uiState.isError) {
            viewModel.fetchPersonalFm()
        }
    }

    BackHandler {
        // 1. 同步刷新 bounds + 设 phase=COLLAPSING，overlay 立即变不透明覆盖住页面切换
        cardAnimState.prepareCollapse()
        // 2. 导航切回 Home（instant，因为 popTransitionSpec 检测到 EntryHomeDetail）
        onBack()
        // 3. 协程跑弹簧收起动画
        coroutineScope.launch { cardAnimState.runCollapse() }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        // Background Layer
        if (id == 0) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF512F),
                                Color(0xFFDD2476).copy(alpha = 0.8f),
                                colorScheme.background,
                                colorScheme.background
                            )
                        )
                    )
                )
            } else if (id == 1) {
                val topSong = uiState.personalFmSongs.lastOrNull()
                AnimatedContent(
                    targetState = topSong?.al?.picUrl,
                    transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
                    label = "fm_bg"
                ) { picUrl ->
                    if (picUrl != null) {
                        AsyncImage(
                            model = picUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(80.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1CB5E0),
                                        Color(0xFF000851).copy(alpha = 0.8f),
                                        colorScheme.background,
                                        colorScheme.background
                                    )
                                )
                            )
                        )
                    }
                }
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            } else {
                Box(Modifier.fillMaxSize().background(colorScheme.background))
            }

            // Content Layer
            if (id == 0) {
                // Daily Recommend Detail
                DailyRecommendContent(
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                    uiState = uiState,
                    isLoggedIn = isLoggedIn,
                    requireAuth = requireAuth,
                    colorScheme = colorScheme
                )
            } else if (id == 1) {
                // Personal FM Detail
                PersonalFmContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    isLoggedIn = isLoggedIn,
                    requireAuth = requireAuth,
                    colorScheme = colorScheme
                )
            } else {
                // Placeholder for other cards
                Box(
                    modifier = Modifier.fillMaxSize().statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Hi, this is HomeDetail for Card $id",
                        color = colorScheme.onBackground
                    )
                }
            }
        }
    }

@Composable
private fun DailyRecommendContent(
    viewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    uiState: HomeUiState,
    isLoggedIn: Boolean,
    requireAuth: () -> Unit,
    colorScheme: ColorScheme
) {
    if (!isLoggedIn || uiState.needsLogin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (uiState.needsLogin) stringResource(id = R.string.login_expired) else stringResource(id = R.string.login_view_recs),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { requireAuth() }) {
                Text(if (uiState.needsLogin) stringResource(id = R.string.relogin) else stringResource(id = R.string.login_now))
            }
        }
    } else if (uiState.isLoading && uiState.recommendSongs.isEmpty()) {
        DailyRecommendSkeleton(colorScheme)
    } else if (uiState.isError && uiState.recommendSongs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.errorMessage?.asString() ?: stringResource(id = R.string.load_failed),
                color = colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { viewModel.fetchRecommendSongs() }) {
                Text(stringResource(id = R.string.retry))
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.statusBarsPadding())
                Column(modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)) {
                    Text(
                        text = "DAILY RECOMMEND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.daily_recommend),
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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.daily_recommend_desc),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }

            items(uiState.recommendSongs, key = { it.id }) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .bouncyClickable {
                            val index = uiState.recommendSongs.indexOf(song)
                            if (index >= 0) {
                                playerViewModel.playSongs(uiState.recommendSongs, index)
                            }
                        }
                        .background(colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.al?.picUrl,
                        contentDescription = "Song Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.name ?: stringResource(id = R.string.unknown_song),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = song.artistName + (song.reason?.let { " · $it" } ?: ""),
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.dislikeSong(song.id) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dislike",
                            tint = colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }
    }
}

@Composable
private fun DailyRecommendSkeleton(colorScheme: ColorScheme) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.statusBarsPadding())
            Box(Modifier.width(160.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
            Spacer(Modifier.height(16.dp))
        }
        items(10) {
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(56.dp)) {
                    Box(Modifier.width(150.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
        }
    }
}

@Composable
private fun PersonalFmContent(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    isLoggedIn: Boolean,
    requireAuth: () -> Unit,
    colorScheme: ColorScheme
) {
    if (!isLoggedIn || uiState.needsLogin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (uiState.needsLogin) stringResource(id = R.string.login_expired) else stringResource(id = R.string.login_listen_fm),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { requireAuth() }) {
                Text(if (uiState.needsLogin) stringResource(id = R.string.relogin) else stringResource(id = R.string.login_now))
            }
        }
    } else if (uiState.isLoading && uiState.personalFmSongs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(id = R.string.tuning), color = Color.White.copy(alpha = 0.8f))
        }
    } else if (uiState.isError && uiState.personalFmSongs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.errorMessage?.asString() ?: stringResource(id = R.string.load_failed),
                color = colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { viewModel.fetchPersonalFm() }) {
                Text(stringResource(id = R.string.retry))
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.statusBarsPadding())
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 16.dp)) {
                Text(
                    text = "PERSONAL FM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(8.dp))
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.private_fm_desc),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.15f),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
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
                // To display top 3 cards, we reverse so index 0 is on top
                val displaySongs = uiState.personalFmSongs.take(3).reversed()
                
                displaySongs.forEachIndexed { index, song ->
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
                        onSwiped = { direction ->
                            if (direction == SwipeDirection.Left) {
                                // Skip
                                viewModel.popFmSong()
                            } else {
                                // Like
                                viewModel.popFmSong()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp) // Adjust height as needed
                            .offset(y = (animatedYOffsetBase + enterAnim.value).dp)
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
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = song.name ?: stringResource(id = R.string.unknown_song),
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
                }
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
                        .clip(RoundedCornerShape(32.dp))
                        .background(colorScheme.surface.copy(alpha = 0.3f))
                        .bouncyClickable { viewModel.popFmSong() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFF000851),
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Like Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(colorScheme.surface.copy(alpha = 0.3f))
                        .bouncyClickable { viewModel.popFmSong() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color(0xFFFF512F),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}