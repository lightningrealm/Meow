package com.lr.meow.ui.common.component.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lr.meow.data.TopBarMenuItem
import com.lr.meow.ui.theme.LocalTopBarMenuItems

@Composable
fun FloatingTopBar(
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val dropMenuItems: List<TopBarMenuItem> = LocalTopBarMenuItems.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 仅在此处加状态栏高度，防止被状态栏遮挡
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(modifier),
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

        var expanded by remember { mutableStateOf(false) }
        Box {
            // 圆形半透明更多键
            CircleFrostedGlassButton(
                graphicsLayer = graphicsLayer,
                icon = Icons.Default.MoreVert,
                modifier = Modifier
                    .size(40.dp)
            ) {
                expanded = true
            }
            GlassDropMenu(
                graphicsLayer,
                expanded,
                {
                    expanded = false
                }
            ) {
                dropMenuItems.forEach { item ->
                    DropdownMenuItem(
                        text = {
                          Text(
                              text = item.label,
                              fontSize = 15.sp,
                              fontWeight = FontWeight.Bold,
                              color = it
                          )
                        },
                        leadingIcon = {
                            Icon(
                                item.icon,
                                item.label,
                                tint = it,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = item.onClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
