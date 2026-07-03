package com.lr.meow.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.glassui.model.GlassEnvironment
import com.lr.meow.R
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
    val targetColorValue = if (glassEnv.isDark) {
        Color.White.copy(alpha = 0.6f)
    } else {
        Color.Black.copy(alpha = 0.5f)
    }
    val dominantColorValue = glassEnv.dominantColor
    val targetColor by animateColorAsState(
        targetColorValue
    )
    val dominantColor by animateColorAsState(
        dominantColorValue
    )
    var showLyrics by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    if (dragAmount.y > 50f) {
                        onBack()
                    }
                }
            }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = targetColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(id = R.string.now_playing),
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

            Spacer(modifier = Modifier.height(32.dp))

            val currentSong by viewModel.currentMediaItem.collectAsState()
            val playbackState by viewModel.playbackState.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()
            val currentPosition by viewModel.currentPosition.collectAsState()
            val duration by viewModel.duration.collectAsState()

            // Big Cover or Lyrics
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "LyricsToggle"
            ) { isLyrics ->
                if (isLyrics) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "歌词功能开发中...",
                            color = targetColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(32.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            // Title & Artist
            Text(
                text = currentSong?.mediaMetadata?.title?.toString() ?: "No Song Playing",
                color = targetColor,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentSong?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
                color = targetColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Progress Bar
            var dragProgress by remember { mutableStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }
            val displayProgress = if (isDragging) dragProgress else (if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPosition.formatTime(),
                    color = targetColor,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                MeowSlider(
                    progress = displayProgress,
                    onProgressChanged = { fraction ->
                        isDragging = true
                        dragProgress = fraction
                    },
                    onProgressChangeFinished = {
                        isDragging = false
                        viewModel.seekTo((dragProgress * duration).toLong())
                    },
                    modifier = Modifier.weight(1f),
                    activeColor = targetColor,
                    inactiveColor = targetColor.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = duration.formatTime(),
                    color = targetColor,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = targetColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(dominantColor)
                        .clickable {
                            if (isPlaying) viewModel.pause() else viewModel.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            scaleIn() togetherWith scaleOut()
                        },
                        label = "PlayPauseToggle"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = targetColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = targetColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
