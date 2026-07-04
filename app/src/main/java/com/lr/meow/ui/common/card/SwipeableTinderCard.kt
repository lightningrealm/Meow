package com.lr.meow.ui.common.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SwipeDirection {
    Left, Right
}

@Composable
fun SwipeableTinderCard(
    onSwiped: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidth * 0.35f
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    Box(
        modifier = modifier
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .graphicsLayer(
                rotationZ = offset.value.x / 20f
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val targetX = offset.value.x
                            if (targetX > swipeThreshold) {
                                // Swipe Right
                                offset.animateTo(Offset(screenWidth * 1.5f, offset.value.y))
                                onSwiped(SwipeDirection.Right)
                            } else if (targetX < -swipeThreshold) {
                                // Swipe Left
                                offset.animateTo(Offset(-screenWidth * 1.5f, offset.value.y))
                                onSwiped(SwipeDirection.Left)
                            } else {
                                // Snap back
                                offset.animateTo(Offset.Zero)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offset.snapTo(
                                Offset(
                                    offset.value.x + dragAmount.x,
                                    offset.value.y + dragAmount.y
                                )
                            )
                        }
                    }
                )
            }
    ) {
        content()
    }
}
