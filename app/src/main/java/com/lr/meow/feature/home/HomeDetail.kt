package com.lr.meow.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.meow.LocalIsLogin
import com.lr.meow.LocalRequireAuth
import com.lr.meow.feature.player.PlayerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
        when (id) {
            0 -> {
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
            }

            1 -> {
                val fmQueue by playerViewModel.fmQueue.collectAsState()
                val topSong = fmQueue.firstOrNull()
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
            }

            else -> {
                Box(Modifier.fillMaxSize().background(colorScheme.background))
            }
        }

            // Content Layer
        when (id) {
            0 -> {
                // Daily Recommend Detail
                DailyRecommendContent(
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                    uiState = uiState,
                    isLoggedIn = isLoggedIn,
                    requireAuth = requireAuth,
                    colorScheme = colorScheme
                )
            }
            1 -> {
                // Personal FM Detail
                PersonalFmContent(
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                    uiState = uiState,
                    isLoggedIn = isLoggedIn,
                    requireAuth = requireAuth,
                    colorScheme = colorScheme
                )
            }
            else -> {
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
    }
