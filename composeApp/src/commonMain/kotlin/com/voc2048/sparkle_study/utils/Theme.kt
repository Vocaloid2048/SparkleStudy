package com.voc2048.sparkle_study.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * SparkleStudy 核心配色方案 (Option 1: 清新療癒風)
 */
object SparkleColors {
    val Primary = Color(0xFF59B292)      // 翡翠綠 (主色)
    val Secondary = Color(0xFFFFC94D)    // 暖金黃 (獎勵/次要)
    val Background = Color(0xFFFFFAF7)   // 杏仁奶油 (背景)
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

val HazeBlurDp10 = HazeStyle(Color.Black, null, 10.dp, 0f)
val HazeBlurDp10Alpha = HazeStyle(Color.Transparent, null, 10.dp, 0f)
val HazeBlurDp20Alpha = HazeStyle(Color.Transparent, null, 20.dp, 0f)
val HazeBlurDp20 = HazeStyle(Color.Black, null, 20.dp, 0f)

fun Modifier.hazeEffectSparkle(
    state: HazeState,
    isBlur: MutableState<Boolean> = mutableStateOf(true),
    isProgressive: Boolean = false,
    style: HazeStyle = HazeBlurDp10,
    block: (HazeEffectScope.() -> Unit)? = {
        blurEnabled = isBlur.value
        progressive = if(isProgressive) HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f) else null
        //mask = Brush.verticalGradient(colors = listOf(Color.Black, Color.Transparent), tileMode = TileMode.Decal)
    },
): Modifier = this.hazeEffect(
    state = state,
    style = style,
    block = block
)

/**
 * 根據背景顏色計算合適的文字顏色 (對比色)。
 */
fun getContrastColor(backgroundColor: Color): Color {
    // 亮度計算：0.299R + 0.587G + 0.114B
    val luminance = 0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue
    return if (luminance > 0.5) Color.Black else Color.White
}

@Composable
fun SparkleStudyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val autoContentColor = getContrastColor(colorScheme.background)

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides autoContentColor
            ) {
                content()
            }
        }
    )
}
