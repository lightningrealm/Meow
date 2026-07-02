package com.lr.meow.ui.common.component.glass

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lr.glassui.glassBlurBackground
import com.lr.glassui.model.GlassEnvironment
import com.lr.meow.data.navigation.MyNavTab
import com.lr.meow.data.navigation.myNavTabs
import com.lr.meow.ui.theme.LocalIsMusicPlaying

@SuppressLint("RestrictedApi")
@Composable
fun CustomFrostedGlassBottomBar(
    modifier: Modifier = Modifier,
    currentTab: MyNavTab,
    isBottomBarVisible: Boolean = true,
    isExpanded: Boolean = false,
    graphicsLayer: GraphicsLayer,
    onTabSelected: (MyNavTab) -> Unit,
    onMiniPlayerClick: () -> Unit = {},
    playerScreenContent: @Composable (glassEnv: GlassEnvironment) -> Unit = {}
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
    // 1. 动态玻璃底色：在基础黑白玻璃上，融入 20% 的环境主色调
    val targetGlassTint = remember(glassEnv) {
        val baseGlass = if (glassEnv.isDark) Color(0xFF111111) else Color(0xFFEEEEEE)
        lerp(baseGlass, glassEnv.dominantColor, 0.2f)
    }
    val glassTint by animateColorAsState(
        targetValue = targetGlassTint.copy(alpha = if (glassEnv.isDark) 0.5f else 0.7f),
        animationSpec = tween(500)
    )

    // 2. 动态高光边框 (Rim Light)
    val borderLight by animateColorAsState(
        targetValue = glassEnv.dominantColor.copy(alpha = if (glassEnv.isDark) 0.6f else 0.25f),
        animationSpec = tween(500)
    )

    val isMusicPlaying = LocalIsMusicPlaying.current
    val tabs = myNavTabs

    val bounciness = 0.65f // Q弹程度 (越小越弹，默认 1.0 不弹)
    val speed = Spring.StiffnessMedium // 收缩速度

    val animatedPadding by animateDpAsState(
        if (isExpanded) 0.dp else 24.dp,
        spring(dampingRatio = bounciness, stiffness = speed)
    )
    val animatedRadius by animateDpAsState(
        if (isExpanded) 0.dp else 32.dp,
        spring(dampingRatio = bounciness, stiffness = speed)
    )
    val bottomInsets = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val animatedBottomPadding by animateDpAsState(
        if (isExpanded) 0.dp else bottomInsets,
        spring(dampingRatio = bounciness, stiffness = speed)
    )

    // 核心改造：使用 Box 将播放器和导航栏封装在“同一块玻璃”内，手动管理尺寸与透明度
    Box(
        modifier = Modifier
            .padding(horizontal = animatedPadding.coerceAtLeast(0.dp))
            .padding(bottom = animatedBottomPadding.coerceAtLeast(0.dp))
            .fillMaxWidth()
            .clip(RoundedCornerShape(animatedRadius.coerceAtLeast(0.dp)))
            // AGSL 玻璃截取：整个大容器只渲染一次 Shader
            .glassBlurBackground(graphicsLayer, blurRadius = 15f) { glassEnviroment ->
                glassEnv = glassEnviroment
            }
            .background(glassTint)
            .border(
                width = 0.8.dp,
                color = borderLight,
                shape = RoundedCornerShape(animatedRadius.coerceAtLeast(0.dp))
            )
            // 流体变形动画核心：完全由外壳控制尺寸
            .animateContentSize(
                if (isExpanded)
                    tween(300, easing = FastOutSlowInEasing)
                else
                    spring(dampingRatio = bounciness, stiffness = speed)
            )
            .run { if (isExpanded) fillMaxHeight() else this }
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        val playerAlpha by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(if (isExpanded) 500 else 200)
        )
        val miniAlpha by animateFloatAsState(
            targetValue = if (isExpanded) 0f else 1f,
            animationSpec = tween(if (isExpanded) 200 else 500)
        )

        // --- 全屏播放器部分 ---
        // 使用 matchParentSize，这样它就不会在消失时撑开 Box，而是被动拉伸/压缩，实现原生级流体变形
        if (isExpanded || playerAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { this.alpha = playerAlpha }
            ) {
                playerScreenContent(glassEnv)
            }
        }

        // --- 底部导航栏与迷你播放器部分 ---
        if (!isExpanded || miniAlpha > 0f) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = miniAlpha }
            ) {
                // --- 迷你播放器部分 ---
                AnimatedVisibility(
                    visible = isMusicPlaying
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMiniPlayerClick
                        )
                    ) {
                        GlassMiniPlayer(glassEnv, borderLight)
                    }
                }

                // --- 底部导航栏部分 ---
                AnimatedVisibility(
                    visible = isBottomBarVisible
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { (tabTarget, outlinedIcon, icon, name) ->
                            val isSelected = currentTab == tabTarget
                            val interactionSource = remember { MutableInteractionSource() }
                            // 环境感知与反色配色
                            val targetColor = if (glassEnv.isDark) {
                                Color.White.copy(alpha = if (isSelected) 1f else 0.6f)
                            } else {
                                Color.Black.copy(alpha = if (isSelected) 1f else 0.5f)
                            }

                            val itemColor by animateColorAsState(
                                targetValue = targetColor,
                                animationSpec = tween(400)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) { onTabSelected(tabTarget) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) icon else outlinedIcon,
                                    contentDescription = name,
                                    tint = itemColor
                                )
                                Text(
                                    text = name,
                                    color = itemColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassMiniPlayer(
    glassEnv: GlassEnvironment,
    borderLight: Color
) {
    val albumTint by animateColorAsState(
        targetValue = glassEnv.dominantColor.copy(alpha = 0.8f),
        animationSpec = tween(200)
    )
    Column {
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
                    // 未来可以换成 Image 组件
                    .background(albumTint)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "The Weekend Mix",
                    color = if (glassEnv.isDark) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Various Artists",
                    color = if (glassEnv.isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = if (glassEnv.isDark) Color.White else Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        // 极简发光进度条（位于播放器和导航栏交界处）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 16.dp)
                .background(borderLight.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f) // 模拟 30% 进度
                    .background(if (glassEnv.isDark) Color.White else Color.Black)
            )
        }
    }
}