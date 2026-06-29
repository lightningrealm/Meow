package com.lr.meow.feature.home

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.lr.meow.ui.theme.LocalSharedTransitionScope

@Composable
fun HomeDetail(id: Int){
    val sharedScope = LocalSharedTransitionScope.current?:return
    val animatedScope = LocalNavAnimatedContentScope.current
    with(sharedScope){
        Column(
            Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("card_hero_$id"),
                    animatedVisibilityScope = animatedScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp))
                )
                .background(Color.White)
                .statusBarsPadding()
        ) {
            Text(
                "Hi,this is HomeDetail"
            )
        }
    }
}