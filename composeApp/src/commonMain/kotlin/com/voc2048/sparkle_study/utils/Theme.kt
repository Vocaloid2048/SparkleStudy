package com.voc2048.sparkle_study.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

val SparkleColorScheme = darkColorScheme(
    primary = Color(0xFF59B292),      // 翡翠綠 (主色)
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF59B292).copy(alpha = 0.2f),
    secondary = Color(0xFFFFC94D),    // 暖金黃 (獎勵/次要)
    onSecondary = Color(0xFF422C00),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFFFF0E5),   // 杏仁奶油 (背景)
    surface = Color(0xFF2D3033),
    onSurface = Color(0xFFE2E2E6),
    error = Color(0xFFFA6781),       // 珊瑚紅 (點綴/錯誤)
    outline = Color(0xFF374141).copy(alpha = 0.5f)
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
    },
): Modifier = this.hazeEffect(
    state = state,
    style = style,
    block = block
)

@Composable
fun SparkleStudyTheme(
    darkTheme: Boolean = false, // 忽略系統深色模式，使用統一色調
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SparkleColorScheme,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides SparkleColorScheme.onBackground
            ) {
                content()
            }
        }
    )
}
