package com.lr.animation.diysharedelement.modifier

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.state.CardAnimState

fun Modifier.registerBounds(state: CardAnimState, cardId: String): Modifier =
    this.onLayoutRectChanged { rectInfo ->
        state.boundsMap[cardId] = rectInfo.boundsInWindow
    }

@Composable
fun SharedElement(
    cardId: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = LocalCardAnimState.current
    val currentContent by rememberUpdatedState(content)

    DisposableEffect(cardId, state) {
        val wrapper: @Composable () -> Unit = { currentContent() }
        state.overlayMap[cardId] = wrapper
        onDispose {
            state.overlayMap.remove(cardId)
        }
    }

    // 自动为外层包裹 Box 并注册位置
    Box(modifier = modifier.registerBounds(state, cardId)) {
        content()
    }
}

/**
 * 在 detail 页中标记动画的目标元素。
 * 当此元素完成 layout 后，会把真实的屏幕坐标通过 updateTarget() 反馈给动画系统，
 * 覆盖 prepareExpand 时传入的估算坐标。不向 boundsMap 写入，不干扰 collapse 时的 source 读取。
 */
@Composable
fun SharedElementTarget(
    cardId: String,
    cornerRadius: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = LocalCardAnimState.current
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    Box(
        modifier = modifier.onLayoutRectChanged { rectInfo ->
            state.updateTarget(cardId, rectInfo.boundsInWindow, cornerRadiusPx)
        }
    ) {
        content()
    }
}