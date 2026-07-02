package com.lr.animation.diysharedelement.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.lr.animation.diysharedelement.model.CardAnimPhase
import com.lr.animation.diysharedelement.state.CardAnimState

val LocalCardAnimState = compositionLocalOf<CardAnimState> {
    error("No CardAnimState provided")
}

@Composable
fun CardAnimRoot(
    state: CardAnimState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCardAnimState provides state) {
        Box(modifier = modifier) {
            content()

            if (state.phase != CardAnimPhase.IDLE) {
                val session = state.session
                if (session != null) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    
                    // 为了避免每一帧都触发昂贵的 layout passes，我们固定 Box 的尺寸为 source 的尺寸，
                    val baseWidth = session.sourceTransform.width
                    val baseHeight = session.sourceTransform.height

                    Box(
                        modifier = Modifier
                            .zIndex(Float.MAX_VALUE)
                            .graphicsLayer {
                                // 核心优化：在这个 graphicsLayer 闭包内部读取 progress 和 currentTransform！
                                // 这意味着哪怕它们每秒变动 120 次，也只会触发极其廉价的 Draw 阶段，
                                // 绝对不会触发 CardAnimRoot 以及内部 NavDisplay 的 Recomposition（重组）！
                                val currentTransform = state.currentTransform
                                if (currentTransform != null) {
                                    val progress = session.progress.value
                                    val overlayAlpha = when (state.phase) {
                                        CardAnimPhase.EXPANDING -> {
                                            val mappedProgress = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
                                            (1f - mappedProgress)
                                        }
                                        CardAnimPhase.COLLAPSING -> 1f
                                        else -> 0f
                                    }

                                    val scaleX = if (baseWidth > 0) currentTransform.width / baseWidth else 1f
                                    val scaleY = if (baseHeight > 0) currentTransform.height / baseHeight else 1f

                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                    translationX = currentTransform.x
                                    translationY = currentTransform.y
                                    this.scaleX = scaleX
                                    this.scaleY = scaleY
                                    clip = true
                                    // 由于 GPU 缩放也会缩放圆角，我们需要给出一个反向缩放的本地圆角，这样缩放后视觉上就是正确的圆角
                                    val localCornerRadius = currentTransform.cornerRadius / scaleX
                                    shape = RoundedCornerShape(localCornerRadius)
                                    alpha = overlayAlpha
                                }
                            }
                            .size(
                                width = with(density) { baseWidth.toDp() },
                                height = with(density) { baseHeight.toDp() }
                            )
                    ) {
                        state.overlayMap[session.cardAnimId]?.invoke()
                    }
                }
            }
        }
    }
}
