package com.lr.meow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue80,
    secondary = SkyBlueGrey80,
    tertiary = SeaGreen80,

    // 深色模式下的背景，带一点点极暗的深蓝色，比纯黑更护眼
    background = Color(0xFF101418),
    surface = Color(0xFF101418),
    onPrimary = Color(0xFF00344F), // 按钮上的文字颜色
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue40,
    secondary = SkyBlueGrey40,
    tertiary = SeaGreen40,

    // 浅色模式下的背景，带一点点极淡的蓝灰，比纯白看起来更柔和高级
    background = Color(0xFFF6FAFE),
    surface = Color(0xFFF6FAFE),
    onPrimary = Color.White, // 按钮上的文字颜色
    onBackground = Color(0xFF171C1F),
    onSurface = Color(0xFF171C1F)
)

@Composable
fun MeowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}