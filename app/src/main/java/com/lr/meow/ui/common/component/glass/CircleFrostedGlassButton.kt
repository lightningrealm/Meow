package com.lr.meow.ui.common.component.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.lr.glassui.glassBlurBackground
import com.lr.glassui.model.GlassEnvironment

@Composable
fun CircleFrostedGlassButton(
    modifier: Modifier = Modifier,
    graphicsLayer: GraphicsLayer,
    icon: ImageVector = Icons.Default.TipsAndUpdates,
    onClick: () -> Unit
) {
    var glassEnv by remember {
        mutableStateOf(
            GlassEnvironment(
                dominantColor = Color.White,
                luminance = (-1f).toDouble(),
                isDark = false
            )
        )
    }
    val targetGlassTint = remember(glassEnv) {
        val baseGlass = if (glassEnv.isDark) Color(0xFF111111) else Color(0xFFEEEEEE)
        lerp(baseGlass, glassEnv.dominantColor, 0.2f)
    }
    val glassTint by animateColorAsState(
        targetValue = targetGlassTint.copy(alpha = if (glassEnv.isDark) 0.5f else 0.7f),
        animationSpec = tween(500)
    )

    // 3. 动态高光边框 (Rim Light)
    val borderLight by animateColorAsState(
        targetValue = (if (glassEnv.isDark) Color.White else Color.Black)
            .copy(alpha = if (glassEnv.isDark) 0.6f else 0.25f),
        animationSpec = tween(500)
    )

    val onGlassColor by animateColorAsState(
        targetValue = if (glassEnv.isDark) {
            Color.White.copy(alpha = 0.5f)
        } else {
            Color.Black.copy(alpha = 0.3f)
        },
        animationSpec = tween(500)
    )

    val radius = remember { 20.dp }
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(2*radius)
            .height(2*radius)
            .clip(RoundedCornerShape(radius))
            .glassBlurBackground(
                graphicsLayer,
                blurRadius = 0f,
                cornerRadiusPx = with(density){20.dp.toPx()},
            ) { glassEnvironment ->
                glassEnv = glassEnvironment
            }
            .background(glassTint)
            // 4. 动态环境光边框：使用带透明度的环境主色调，打造物理倒角折射高光
            .border(
                width = 0.8.dp,
                color = borderLight,
                shape = RoundedCornerShape(radius)
            )
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            icon.toString(),
            tint = onGlassColor
        )
    }
}