package com.voc2048.sparkle_study.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SparkleStudy 核心配色方案 (Option 1: 清新療癒風)
 */
object SparkleColors {
    val Primary = Color(0xFF59B292)      // 翡翠綠 (主色)
    val Secondary = Color(0xFFFFC94D)    // 暖金黃 (獎勵/次要)
    val Background = Color(0xFFFAE7CB)   // 杏仁奶油 (背景)
    val Accent = Color(0xFFFA6781)       // 珊瑚紅 (點綴/錯誤)
    
    // 暗色模式適配
    val DarkPrimary = Color(0xFF82CBB2)
    val DarkSecondary = Color(0xFFFFD680)
    val DarkBackground = Color(0xFF1A1C1E)
    val DarkSurface = Color(0xFF2D3033)
}

private val LightColorScheme = lightColorScheme(
    primary = SparkleColors.Primary,
    onPrimary = Color.White,
    primaryContainer = SparkleColors.Primary.copy(alpha = 0.2f),
    secondary = SparkleColors.Secondary,
    onSecondary = Color(0xFF422C00),
    background = SparkleColors.Background,
    onBackground = Color(0xFF1A1C1E),
    surface = SparkleColors.Background,
    onSurface = Color(0xFF1A1C1E),
    error = SparkleColors.Accent,
    outline = SparkleColors.Primary.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = SparkleColors.DarkPrimary,
    onPrimary = Color(0xFF003828),
    primaryContainer = SparkleColors.DarkPrimary.copy(alpha = 0.2f),
    secondary = SparkleColors.DarkSecondary,
    onSecondary = Color(0xFF422C00),
    background = SparkleColors.DarkBackground,
    onBackground = Color(0xFFE2E2E6),
    surface = SparkleColors.DarkSurface,
    onSurface = Color(0xFFE2E2E6),
    error = SparkleColors.Accent,
    outline = SparkleColors.DarkPrimary.copy(alpha = 0.5f)
)

@Composable
fun SparkleStudyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        // TODO: 定義 Typography
        content = content
    )
}
