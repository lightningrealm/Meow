package com.lr.meow.ui.common.component.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lr.glassui.glassBlurBackground
import com.lr.glassui.model.GlassEnvironment


@Composable
fun GlassDropMenu(
    graphicsLayer: GraphicsLayer,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable (Color) -> Unit
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
        val baseGlass = if (glassEnv.isDark) Color(0xFF707070) else Color(0xFFCECECE)
        lerp(baseGlass, glassEnv.dominantColor, 0.05f)
    }
    val glassTint by animateColorAsState(
        targetValue = targetGlassTint.copy(alpha = if (glassEnv.isDark) 0.5f else 0.7f),
        animationSpec = tween(500)
    )

    // 3. 动态高光边框 (Rim Light)
    val borderLight by animateColorAsState(
        targetValue = glassEnv.dominantColor.copy(alpha = if (glassEnv.isDark) 0.6f else 0.25f),
        animationSpec = tween(500)
    )
    val onGlassColor by animateColorAsState(
        targetValue = if (glassEnv.isDark) {
            Color.White.copy(alpha = 0.5f)
        } else {
            Color.Black.copy(alpha = 0.5f)
        },
        animationSpec = tween(500)
    )
    val density = LocalDensity.current
    val radius by remember { mutableStateOf(16.dp) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(radius),
        modifier = Modifier
            .glassBlurBackground(
                graphicsLayer,
                blurRadius = 15f,
                cornerRadiusPx = with(density){radius.toPx()},
            ){ environment ->
                glassEnv = environment
            }
            .background(glassTint)
            .border(
                width = 0.8.dp,
                color = borderLight,
                shape = RoundedCornerShape(radius)
            )
    ) {
        content(onGlassColor)
    }
}