package com.lr.meow.ui.common.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun MeowSlider(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.3f)
) {
    var width by remember { mutableStateOf(1) } // Default to 1 to avoid divide by zero
    var isDragging by remember { mutableStateOf(false) }

    val trackHeight = if (isDragging) 6.dp else 4.dp
    val animatedTrackHeight by animateDpAsState(
        targetValue = trackHeight,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp) // Touch target size
            .onSizeChanged { 
                if (it.width > 0) width = it.width 
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / width.toFloat()).coerceIn(0f, 1f)
                        onProgressChanged(newProgress)
                        
                        val isReleased = tryAwaitRelease()
                        isDragging = false
                        if (isReleased) {
                            onProgressChangeFinished()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / width.toFloat()).coerceIn(0f, 1f)
                        onProgressChanged(newProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onProgressChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                        onProgressChangeFinished()
                    },
                    onHorizontalDrag = { change, _ ->
                        val newProgress = (change.position.x / width.toFloat()).coerceIn(0f, 1f)
                        onProgressChanged(newProgress)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Inactive track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedTrackHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(inactiveColor)
        )
        // Active track
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(animatedTrackHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(activeColor)
        )
    }
}
