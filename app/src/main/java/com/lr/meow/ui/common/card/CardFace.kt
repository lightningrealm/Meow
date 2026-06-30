package com.lr.meow.ui.common.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.lr.meow.data.cardface.CardFaceData
import com.lr.meow.ui.theme.LocalSharedTransitionScope

@Composable
fun CardFace(
    cardFaceData: CardFaceData,
    sharedKey: String,
    onClick: () -> Unit = {}
) {
    val sharedScope = LocalSharedTransitionScope.current ?: return
    val animatedScope = LocalNavAnimatedContentScope.current

    val gradient = cardFaceData.backgroundGradient
    val cardShape = RoundedCornerShape(20.dp)

    with(sharedScope) {
        Box(
            Modifier
                .fillMaxSize()
                // 1. 添加弥散阴影 (使用渐变底色作为阴影色，更有质感)
                .shadow(
                    elevation = 12.dp,
                    shape = cardShape,
                    spotColor = gradient.last().copy(alpha = 0.6f),
                    ambientColor = gradient.last().copy(alpha = 0.4f)
                )
                .clip(cardShape)
                // 2. 添加微光内边框 (增加精致感)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = cardShape
                )
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(sharedKey),
                    animatedVisibilityScope = animatedScope,
                    clipInOverlayDuringTransition = OverlayClip(cardShape)
                )
                .clickable { onClick() }
        ) {
            // 背景层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradient))
            )

            // 底部暗角层 (优化了透明度过渡，使其更自然)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) // 占下半部分即可，性能更好
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                gradient.last().copy(alpha = 0.5f),
                                gradient.last().copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // 内容层
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp) // 稍微增大一点水平边距，增加呼吸感
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                // Top Title
                Text(
                    text = cardFaceData.topTitle.uppercase(), // 顶层小标签通常大写更有设计感
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.weight(1f))

                // Main Title
                Text(
                    text = cardFaceData.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold, // 加粗一点让标题更具视觉冲击力
                    lineHeight = 38.sp,
                    color = Color.White,
                    // 3. 添加文字阴影，保证在任何渐变背景下都清晰可读
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = cardFaceData.subTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f), // 稍微提高一点对比度
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.15f),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}