package com.lr.meow.ui.common.component.glass

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lr.glassui.glassBlurBackground
import com.lr.glassui.model.GlassEnvironment
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


    // 3. 动态高光边框 (Rim Light)
    val borderLight by animateColorAsState(
        targetValue = glassEnv.dominantColor.copy(alpha = if (glassEnv.isDark) 0.6f else 0.25f),
        animationSpec = tween(500)
    )

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
            .glassBlurBackground(graphicsLayer, blurRadius = 15f) { glassEnviroment ->
                glassEnv = glassEnviroment
            }
            .background(glassTint)
            // 4. 动态环境光边框：使用带透明度的环境主色调，打造物理倒角折射高光
            .border(
                width = 0.8.dp,
                color = borderLight,
                shape = RoundedCornerShape(32.dp)
            )
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (tabTarget,outlinedIcon,icon, name) ->
            val isSelected = currentTab == tabTarget
            val interactionSource  = remember { MutableInteractionSource() }
            // 环境感知与反色配色：真正的明暗反转（深色底配白字，亮色底配黑字）
            val targetColor = if (glassEnv.isDark) {
                Color.White.copy(alpha = if (isSelected) 1f else 0.6f)
            } else {
                Color.Black.copy(alpha = if (isSelected) 1f else 0.5f)
            }
            
            // 为每个 Tab 注入独立的色彩插值动画（包含点击选中、以及背景流明切换时的平滑过渡）
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