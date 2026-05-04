package com.voc2048.sparkle_study.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 使用用戶提供的 4 個療癒主色調
val PrimaryGreen = Color(0xFFA3DC9A)
val SecondaryOrange = Color(0xFFFFD6BA)
val TertiaryYellowGreen = Color(0xFFDEE791)
val BackgroundYellow = Color(0xFFFFF9BD)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.DarkGray,
    secondary = SecondaryOrange,
    onSecondary = Color.DarkGray,
    tertiary = TertiaryYellowGreen,
    onTertiary = Color.DarkGray,
    background = BackgroundYellow,
    onBackground = Color.DarkGray,
    surface = BackgroundYellow,
    onSurface = Color.DarkGray,
    primaryContainer = PrimaryGreen.copy(alpha = 0.3f),
    onPrimaryContainer = Color.DarkGray,
    secondaryContainer = SecondaryOrange.copy(alpha = 0.3f),
    onSecondaryContainer = Color.DarkGray
)

@Composable
fun SparkleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 暫時以淺色為主，可依需求拓展深色模式
    content: @Composable () -> Unit
) {
    // 依據療癒系風格，暫時統一使用這套亮色/柔和色配置
    val colorScheme = LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

