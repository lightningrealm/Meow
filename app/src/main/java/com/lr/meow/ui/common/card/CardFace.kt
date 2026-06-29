package com.lr.meow.ui.common.card

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.lr.meow.data.cardface.CardFaceData
import com.lr.meow.ui.theme.LocalSharedTransitionScope

import androidx.compose.animation.SharedTransitionScope.OverlayClip
@Composable
fun CardFace(
    cardFaceData: CardFaceData,
    sharedKey: String,
    onClick:()->Unit={}
) {
    val sharedScope = LocalSharedTransitionScope.current?:return
    val animatedScope = LocalNavAnimatedContentScope.current
    val containerSize = LocalWindowInfo.current.containerSize
    val cardWidth = containerSize.width
    val gradient = cardFaceData.backgroundGradient
    with(sharedScope){
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(sharedKey),
                    animatedVisibilityScope = animatedScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(20.dp))
                )
                .clickable {
                    onClick()
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            gradient
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                gradient.last().copy(alpha = 0.72f),
                                gradient.last()
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Column(
                    modifier = Modifier
                        //.requiredWidth(cardWidth)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp)
                        .padding(top = 20.dp, bottom = 22.dp)
                ) {
                    Text(
                        text = cardFaceData.topTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 1.3.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = cardFaceData.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 37.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = cardFaceData.subTitle,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.70f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                }
            }
        }
    }
}