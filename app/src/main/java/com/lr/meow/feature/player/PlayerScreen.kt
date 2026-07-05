package com.lr.meow.feature.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import com.lr.glassui.model.GlassEnvironment
import com.lr.meow.ui.common.component.MeowSlider
import org.koin.androidx.compose.koinViewModel

private fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

@Composable
fun PlayerScreen(
    glassEnv: GlassEnvironment,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = koinViewModel()
) {
    BackHandler { onBack() }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val targetColorValue =
        if (glassEnv.isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
    val dominantColorValue = glassEnv.dominantColor
    val targetColor by animateColorAsState(targetColorValue, label = "targetColor")
    val dominantColor by animateColorAsState(dominantColorValue, label = "dominantColor")

    var showLyrics by rememberSaveable { mutableStateOf(false) }

    // 状态收集
    val currentSong by viewModel.currentMediaItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val lyricState by viewModel.lyricState.collectAsState()
    val shuffleModeEnabled by viewModel.shuffleModeEnable.collectAsState()

    // 根布局
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount -> if (dragAmount.y > 50f) onBack() }
            }
            .systemBarsPadding()
    ) {
        // ✨ 修复 1：最外层只用一个 Column 统筹全局
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Collapse",
                        tint = targetColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "正在播放",
                    color = targetColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showLyrics = !showLyrics }) {
                    Text(
                        text = "词",
                        color = targetColor,
                        fontWeight = if (showLyrics) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }

            if (isLandscape) {
                // --- 横屏模式 ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(), // 填满剩余空间
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverAndLyricsArea(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        showLyrics = showLyrics,
                        lyricState = lyricState,
                        currentPosition = currentPosition,
                        currentSong = currentSong,
                        targetColor = targetColor,
                        isLandscape = true
                    )
                    ControlsArea(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        shuffleModeEnabled = shuffleModeEnabled,
                        targetColor = targetColor,
                        dominantColor = dominantColor,
                        isLandscape = true,
                        viewModel = viewModel
                    )
                }
            } else {
                // --- 竖屏模式 (修复区域) ---
                // 上半部分：封面，使用 weight(1f) 占据所有剩余弹性空间
                CoverAndLyricsArea(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    showLyrics = showLyrics,
                    lyricState = lyricState,
                    currentPosition = currentPosition,
                    currentSong = currentSong,
                    targetColor = targetColor, isLandscape = false
                )

                // 下半部分：控制区，高度自适应包裹内容，被上方封面稳稳压在底部
                ControlsArea(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    currentSong = currentSong, isPlaying = isPlaying,
                    currentPosition = currentPosition, duration = duration,
                    shuffleModeEnabled = shuffleModeEnabled,
                    targetColor = targetColor, dominantColor = dominantColor,
                    isLandscape = false, viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun CoverAndLyricsArea(
    modifier: Modifier,
    showLyrics: Boolean,
    lyricState: LyricUiState,
    currentPosition: Long,
    currentSong: MediaItem?,
    targetColor: Color,
    isLandscape: Boolean
) {
    AnimatedContent(
        targetState = showLyrics,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "LyricsToggle",
        modifier = modifier
    ) { isLyrics ->
        if (isLyrics) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (lyricState) {
                    is LyricUiState.Loading -> CircularProgressIndicator(color = targetColor)
                    is LyricUiState.Error -> Text(
                        text = lyricState.message,
                        color = targetColor,
                        fontSize = 18.sp
                    )

                    is LyricUiState.Success -> {
                        if (lyricState.lyrics.isEmpty()) {
                            Text(text = "纯音乐，请欣赏", color = targetColor, fontSize = 18.sp)
                        } else {
                            val listState = rememberLazyListState()
                            val activeIndex = remember(lyricState.lyrics, currentPosition) {
                                val idx =
                                    lyricState.lyrics.indexOfLast { it.startTimeMs <= currentPosition }
                                if (idx == -1) 0 else idx
                            }
                            LaunchedEffect(activeIndex) {
                                if (activeIndex >= 0) listState.animateScrollToItem(
                                    maxOf(
                                        0,
                                        activeIndex - 3
                                    ), 0
                                )
                            }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(vertical = if (isLandscape) 40.dp else 120.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(lyricState.lyrics.size) { index ->
                                    val line = lyricState.lyrics[index]
                                    val isActive = index == activeIndex
                                    val color by animateColorAsState(
                                        targetValue = if (isActive) targetColor else targetColor.copy(
                                            alpha = 0.5f
                                        ),
                                        animationSpec = tween(300), label = ""
                                    )
                                    val scale by animateFloatAsState(
                                        targetValue = if (isActive) 1.25f else 1f,
                                        animationSpec = tween(300), label = ""
                                    )
                                    Text(
                                        text = line.text,
                                        color = color,
                                        fontSize = 16.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = scale; scaleY = scale
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f, matchHeightConstraintsFirst = isLandscape)
                        .padding(if (isLandscape) 16.dp else 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = currentSong?.mediaMetadata?.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsArea(
    modifier: Modifier,
    currentSong: MediaItem?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    shuffleModeEnabled: Boolean,
    targetColor: Color,
    dominantColor: Color,
    isLandscape: Boolean,
    viewModel: PlayerViewModel
) {
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val displayProgress =
        if (isDragging) dragProgress else (if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f)

    Column(
        modifier = modifier
            .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(vertical = if (isLandscape) 8.dp else 0.dp),
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题和歌手
        Text(
            text = currentSong?.mediaMetadata?.title?.toString()?:"未有播放的歌曲",
            color = targetColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (isLandscape) 20.sp else 24.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currentSong?.mediaMetadata?.artist?.toString()?:"歌手名",
            color = targetColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ✨ 修复 2：将非法 weight 替换为固定安全高度
        Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))

        // 进度条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currentPosition.formatTime(), color = targetColor, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            MeowSlider(
                progress = displayProgress,
                onProgressChanged = { fraction -> isDragging = true; dragProgress = fraction },
                onProgressChangeFinished = {
                    isDragging = false; viewModel.seekTo((dragProgress * duration).toLong())
                },
                modifier = Modifier.weight(1f),
                activeColor = targetColor, inactiveColor = targetColor.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = duration.formatTime(), color = targetColor, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))

        // 播控按钮组
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLandscape) 16.dp else 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                AnimatedContent(
                    targetState = shuffleModeEnabled,
                    transitionSpec = { scaleIn() togetherWith scaleOut() },
                ) { state ->
                    if (state){
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = targetColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(if (isLandscape) 28.dp else 32.dp)
                        )
                    }
                    else{
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = targetColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(if (isLandscape) 28.dp else 32.dp)
                        )
                    }
                }
                /*Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleModeEnabled) targetColor else targetColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(if (isLandscape) 28.dp else 32.dp)
                )*/
            }

            IconButton(onClick = { viewModel.skipToPrevious() }) {
                Icon(
                    Icons.Default.SkipPrevious,
                    "Previous",
                    tint = targetColor,
                    modifier = Modifier.size(if (isLandscape) 40.dp else 48.dp)
                )
            }

            // 播放/暂停按钮
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 64.dp else 80.dp)
                    .clip(CircleShape)
                    .background(dominantColor)
                    .clickable { if (isPlaying) viewModel.pause() else viewModel.play() },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = { scaleIn() togetherWith scaleOut() },
                    label = "PlayPauseToggle"
                ) { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = targetColor,
                        modifier = Modifier.size(if (isLandscape) 32.dp else 40.dp)
                    )
                }
            }

            IconButton(onClick = { viewModel.skipToNext() }) {
                Icon(
                    Icons.Default.SkipNext,
                    "Next",
                    tint = targetColor,
                    modifier = Modifier.size(if (isLandscape) 40.dp else 48.dp)
                )
            }

            Spacer(modifier = Modifier.size(if (isLandscape) 28.dp else 32.dp))
        }
    }
}