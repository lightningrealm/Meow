package com.lr.meow.ui.common

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lr.glassui.glassBlurBackground
import com.lr.meow.data.navigation.MyNavTab
import com.lr.meow.data.navigation.NavQuadruple

@SuppressLint("RestrictedApi")
@Composable
fun CustomFrostedGlassBottomBar(
    modifier: Modifier = Modifier,
    currentTab: MyNavTab,
    graphicsLayer: GraphicsLayer,
    onTabSelected: (MyNavTab)-> Unit
){
    var isDarkBackground by remember { mutableStateOf(false) }
    val animatedColor by animateColorAsState(
        targetValue = if(isDarkBackground) Color.White else Color.Black,
        animationSpec = tween(500)
    )
    val reverseAnimatedColor by animateColorAsState(
        targetValue = if(isDarkBackground) Color.Black else Color.White,
        animationSpec = tween(500)
    )

    // 动态遮罩：背景暗时铺白底，背景亮时铺黑底，保证对比度
    val maskColors = if (isDarkBackground) {
        listOf(
            animatedColor.copy(alpha = 0.5f), // 加重，让底栏在深色背景上浮现
            animatedColor.copy(alpha = 0.3f)
        )
    } else {
        listOf(
            animatedColor.copy(alpha = 0.5f),
            animatedColor.copy(alpha = 0.4f)
        )
    }

    val tabs = listOf(
        NavQuadruple(
            MyNavTab.HOME,
            Icons.Outlined.Home,
            Icons.Default.Home,
            "主页"
        ),
        NavQuadruple(
            MyNavTab.DISCOVER,
            Icons.Outlined.Explore,
            Icons.Default.Explore,
            "发现"
        ),
        NavQuadruple(
            MyNavTab.LIBRARY,
            Icons.Outlined.LibraryMusic,
            Icons.Default.LibraryMusic,
            "音乐库"
        ),
        NavQuadruple(
            MyNavTab.SEARCH,
            Icons.Outlined.Search,
            Icons.Default.Search,
            "搜索"
        )
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            //.padding(bottom = 16.dp)
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .glassBlurBackground(graphicsLayer, blurRadius = 15f) { isDark ->
                isDarkBackground = isDark
            }
            .background(Brush.verticalGradient(colors = maskColors))
            // 4. 灵魂注入：加一圈半透明的白色边框，模拟玻璃的边缘反光！
            /*.border(
                width = 0.8.dp,
                // 深色背景时边框更亮，模拟玻璃在暗处的高光边缘
                color = if (isDarkBackground) animatedColor.copy(alpha = 0.7f) else animatedColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(32.dp)
            )*/
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (tabTarget,outlinedIcon,icon, name) ->
            val isSelected = currentTab == tabTarget
            val interactionSource  = remember { MutableInteractionSource() }
            // reverseAnimatedColor 与遮罩互补，选中项用全不透明，未选中用半透明
            val itemColor = reverseAnimatedColor.copy(
                alpha = when{
                    isSelected -> 1f
                    isDarkBackground -> 0.6f
                    else -> 0.4f
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ){ onTabSelected(tabTarget) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Icon(
                    imageVector = if(isSelected) icon else outlinedIcon,
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