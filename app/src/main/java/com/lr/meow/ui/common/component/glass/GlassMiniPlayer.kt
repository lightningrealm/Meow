package com.lr.meow.ui.common.component.glass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import com.lr.glassui.model.GlassEnvironment

@Composable
fun GlassMiniPlayer(
    glassEnv: GlassEnvironment,
    borderLight: Color,
    currentSong: MediaItem?,
    isPlaying: Boolean,
    progress: Float,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit
) {
    val albumTint by animateColorAsState(
        targetValue = glassEnv.dominantColor.copy(alpha = 0.8f),
        animationSpec = tween(200),
        label = "albumTint"
    )
    var width by remember { mutableIntStateOf(1) }
    var isPressing by remember { mutableStateOf(false) }
    var isHorizontalDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val isInteracting = isPressing || isHorizontalDragging

    val trackHeight = if (isInteracting) 6.dp else 1.dp
    val animatedTrackHeight by animateDpAsState(
        targetValue = trackHeight,
        animationSpec = tween(durationMillis = 200),
        label = "trackHeight"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { if (it.width > 0) width = it.width }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        isPressing = true
                        val isReleased = tryAwaitRelease()
                        isPressing = false
                        if (isReleased) {
                            onClick()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isHorizontalDragging = true
                        dragProgress = (offset.x / width.toFloat()).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isHorizontalDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isHorizontalDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        dragProgress = (change.position.x / width.toFloat()).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 模拟专辑封面
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(albumTint)
            ) {
                AsyncImage(
                    model = currentSong?.mediaMetadata?.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.mediaMetadata?.title?.toString() ?: "No Song Playing",
                    color = if (glassEnv.isDark) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
                    color = if (glassEnv.isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Previous
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = if (glassEnv.isDark) Color.White else Color.Black,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onPrevious() }
                        .padding(4.dp)
                )

                // Play/Pause
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        scaleIn() togetherWith scaleOut()
                    },
                    label = "PlayPauseToggle",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onPlayPause() }
                ) { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = if (glassEnv.isDark) Color.White else Color.Black,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    )
                }

                // Next
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = if (glassEnv.isDark) Color.White else Color.Black,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onNext() }
                        .padding(4.dp)
                )
            }
        }

        // 极简发光进度条（位于播放器和导航栏交界处）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedTrackHeight)
                .padding(horizontal = 16.dp)
                .background(borderLight.copy(alpha = 0.3f))
        ) {
            val displayProgress = if (isHorizontalDragging) dragProgress else progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
                    .background(if (glassEnv.isDark) Color.White else Color.Black)
            )
        }
    }
}
