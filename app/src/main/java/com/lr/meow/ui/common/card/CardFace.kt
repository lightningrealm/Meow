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
import com.lr.meow.data.cardface.CardFaceData

@Composable
fun CardFace(
    cardFaceData: CardFaceData,
    onClick: () -> Unit = {}
) {
    val gradient = cardFaceData.backgroundGradient
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        Modifier
            .fillMaxSize()
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                spotColor = gradient.last().copy(alpha = 0.6f),
                ambientColor = gradient.last().copy(alpha = 0.4f)
            )
            .clip(cardShape)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = cardShape
            )
            .clickable { onClick() }
    ) {
        // 背景渐变层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
        )

        // 底部暗角层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
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
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = cardFaceData.topTitle.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = cardFaceData.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.2f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cardFaceData.subTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
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