package com.lr.animation.diysharedelement.modifier

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onLayoutRectChanged
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