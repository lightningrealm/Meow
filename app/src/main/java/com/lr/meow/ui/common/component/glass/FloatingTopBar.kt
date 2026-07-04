package com.lr.meow.ui.common.component.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun FloatingTopBar(
    graphicsLayer: GraphicsLayer,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 仅在此处加状态栏高度，防止被状态栏遮挡
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 圆形半透明返回键
        CircleFrostedGlassButton(
            graphicsLayer = graphicsLayer,
            icon = Icons.Default.ArrowBackIosNew,
            modifier = Modifier
                .size(40.dp)
        ) {
            onBack()
        }


        // 圆形半透明更多键
        CircleFrostedGlassButton(
            graphicsLayer = graphicsLayer,
            icon = Icons.Default.MoreVert,
            modifier = Modifier
                .size(40.dp)
        ) {

        }
    }
}