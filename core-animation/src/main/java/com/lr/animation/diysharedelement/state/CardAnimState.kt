package com.lr.animation.diysharedelement.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntRect
import com.lr.animation.diysharedelement.model.CardAnimPhase
import com.lr.animation.diysharedelement.model.CardAnimSession
import com.lr.animation.diysharedelement.model.CardAnimTransform
import kotlinx.coroutines.CoroutineScope

class CardAnimState(
    private val sourceCornerRadiusPx: Float,
    val animScope: CoroutineScope
) {
    var phase by mutableStateOf(CardAnimPhase.IDLE)
        private set

    var session by mutableStateOf<CardAnimSession?>(null)
        private set

    internal val boundsMap = mutableStateMapOf<String, IntRect>()
    internal val overlayMap = mutableStateMapOf<String, @Composable () -> Unit>()

    // ── Convenience ───────────────────────────
    /** Card currently owned by the overlay (invisible in list). */
    val animatingCardId: String?
        get() = when (phase) {
            CardAnimPhase.IDLE -> null
            else -> session?.cardAnimId
        }

    /** The current transform based on interpolation */
    val currentTransform: CardAnimTransform?
        get() {
            val s = session ?: return null
            if (phase == CardAnimPhase.IDLE) return null
            return CardAnimTransform.lerp(s.sourceTransform, s.targetTransform, s.progress.value)
        }

    private fun captureSource(cardId: String): CardAnimTransform? {
        val bounds = boundsMap[cardId] ?: return null
        return CardAnimTransform(
            x = bounds.left.toFloat(),
            y = bounds.top.toFloat(),
            width = bounds.width.toFloat(),
            height = bounds.height.toFloat(),
            cornerRadius = sourceCornerRadiusPx
        )
    }



    /**
     * 同步：建立 session、设置 phase = EXPANDING。
     * 在导航之前调用，这样 NavDisplay 的 transitionSpec 能感知到 phase != IDLE。
     * 返回 false 表示找不到该卡片的 bounds（不该导航）。
     */
    fun prepareExpand(
        cardAnimId: String,
        targetTransform: (source: CardAnimTransform) -> CardAnimTransform
    ): Boolean {
        val source = captureSource(cardAnimId) ?: return false
        val target = targetTransform(source)
        val anim = Animatable(0f)
        session = CardAnimSession(cardAnimId, source, target, anim)
        phase = CardAnimPhase.EXPANDING
        return true
    }

    /**
     * 挂起：跑扩展动画直到完成。必须在 prepareExpand 之后调用。
     */
    suspend fun runExpand() {
        try {
            session?.progress?.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } finally {
            phase = CardAnimPhase.EXPANDED
        }
    }
    /** 旧的一体化接口，保留兼容性 */
    suspend fun expand(
        cardAnimId: String,
        targetTransform: (source: CardAnimTransform) -> CardAnimTransform
    ) {
        if (prepareExpand(cardAnimId, targetTransform)) {
            runExpand()
        }
    }

    /**
     * 在 detail 页面渲染完成后，用真实的 bounds 更新 session 的 targetTransform。
     * 不影响 boundsMap（boundsMap 只给 source 注册用），collapse 时仍从 boundsMap 读取 source。
     */
    fun updateTarget(cardId: String, realBounds: IntRect, cornerRadiusPx: Float) {
        val s = session ?: return
        if (s.cardAnimId != cardId) return
        if (phase == CardAnimPhase.IDLE) return
        session = s.copy(
            targetTransform = CardAnimTransform(
                x = realBounds.left.toFloat(),
                y = realBounds.top.toFloat(),
                width = realBounds.width.toFloat(),
                height = realBounds.height.toFloat(),
                cornerRadius = cornerRadiusPx
            )
        )
    }

    /**
     * 同步：刷新 source bounds、设置 phase = COLLAPSING。
     * 在 onBack 之前调用，overlay 立即变为可见，防止页面闪现。
     */
    fun prepareCollapse() {
        session?.let { s ->
            captureSource(s.cardAnimId)?.let { fresh ->
                session = s.copy(sourceTransform = fresh)
            }
        }
        phase = CardAnimPhase.COLLAPSING
    }

    /**
     * 挂起：跑收起动画直到完成。必须在 prepareCollapse 之后调用。
     */
    suspend fun runCollapse() {
        try {
            session?.progress?.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } finally {
            phase = CardAnimPhase.IDLE
            session = null
        }
    }

    /** 旧的一体化接口，保留兼容性 */
    suspend fun collapse() {
        prepareCollapse()
        runCollapse()
    }
}
