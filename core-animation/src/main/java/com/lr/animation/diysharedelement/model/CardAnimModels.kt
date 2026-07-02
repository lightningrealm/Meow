package com.lr.animation.diysharedelement.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D

data class CardAnimTransform(
    val x: Float,             // top-left X, screen pixels
    val y: Float,             // top-left Y, screen pixels
    val width: Float,         // pixels
    val height: Float,        // pixels
    val cornerRadius: Float   // pixels
) {
    companion object {
        fun lerp(from: CardAnimTransform, to: CardAnimTransform, t: Float) = CardAnimTransform(
            x = androidx.compose.ui.util.lerp(from.x, to.x, t),
            y = androidx.compose.ui.util.lerp(from.y, to.y, t),
            width = androidx.compose.ui.util.lerp(from.width, to.width, t),
            height = androidx.compose.ui.util.lerp(from.height, to.height, t),
            cornerRadius = androidx.compose.ui.util.lerp(from.cornerRadius, to.cornerRadius, t)
        )
    }
}

enum class CardAnimPhase {
    IDLE,       // normal list; no overlay
    EXPANDING,  // overlay card animating  0 → 1
    EXPANDED,   // full-screen detail visible
    COLLAPSING  // overlay card animating  1 → 0
}

data class CardAnimSession(
    val cardAnimId: String,
    val sourceTransform: CardAnimTransform,
    val targetTransform: CardAnimTransform,
    val progress: Animatable<Float, AnimationVector1D>
)
