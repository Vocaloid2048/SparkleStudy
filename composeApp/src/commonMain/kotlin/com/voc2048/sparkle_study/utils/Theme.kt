package com.voc2048.sparkle_study.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.composed
import com.voc2048.sparkle_study.SetSystemBarsStyle
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// 全局主題狀態
val appThemeState: MutableState<ThemeMode> by lazy { 
    val saved = try {
        ThemeMode.valueOf(Preferences().themeMode)
    } catch (e: Exception) {
        ThemeMode.SYSTEM
    }
    mutableStateOf(saved)
}

val LocalIsDarkTheme = staticCompositionLocalOf { false }

val LightSparkleColorScheme = lightColorScheme(
    primary = Color(0xFF7CB38D),      // 柔和草綠 (增強對比度)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF2E4D36),
    
    secondary = Color(0xFF7FA8C9),    // 柔和空藍 (增強對比度)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF2C4357),

    tertiary = Color(0xFFA58EC7),     // 柔和薰紫 (增強對比度)
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E5F5),

    background = Color(0xFFFFFDF5),   // 極淡奶油黃
    onBackground = Color(0xFF423E3B), // 深棕色文字 (確保極佳可視性)
    
    surface = Color(0xFFF2F2F2),      // 淺灰色表面 (取代原本的粉黃)
    onSurface = Color(0xFF423E3B),
    
    surfaceVariant = Color(0xFFEBEBEB),
    onSurfaceVariant = Color(0xFF7E746A),

    error = Color(0xFFFF8A8A),       // 粉紅警告色
    onError = Color.White,
    errorContainer = Color(0xFFFFD1D1),
    
    outline = Color(0xFFBDB29A),     // 清晰的輪廓線
    outlineVariant = Color(0xFFEBE3D9)
)

val DarkSparkleColorScheme = darkColorScheme(
    primary = Color(0xFF59B292),      // 翡翠綠
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF59B292).copy(alpha = 0.2f),
    
    secondary = Color(0xFFFFC94D),    // 暖金黃
    onSecondary = Color(0xFF422C00),
    secondaryContainer = Color(0xFF422C00).copy(alpha = 0.3f),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFFFF0E5), // 杏仁奶油
    
    surface = Color(0xFF2D3033),
    onSurface = Color(0xFFE2E2E6),
    
    surfaceVariant = Color(0xFF3F4848),
    onSurfaceVariant = Color(0xFFBFC8C8),

    error = Color(0xFFFA6781),       // 珊瑚紅
    outline = Color(0xFF374141).copy(alpha = 0.5f),
    outlineVariant = Color(0xFF444746)
)

// 方便組件獲取當前 scheme
val SparkleColorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

@Composable
fun getHazeStyle(isDark: Boolean, blur: Int = 10): HazeStyle {
    val color = if (isDark) Color.Black else Color.White
    return HazeStyle(color.copy(alpha = 0.2f), null, blur.dp, 0f)
}


fun Modifier.hazeEffectSparkle(
    state: HazeState,
    isBlur: MutableState<Boolean> = mutableStateOf(true),
    isProgressive: Boolean = false,
    style: HazeStyle? = null,
    block: (HazeEffectScope.() -> Unit)? = {
        blurEnabled = isBlur.value
        progressive = if(isProgressive) HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f) else null
    },
): Modifier = this.composed {
    val isDark = LocalIsDarkTheme.current
    val resolvedStyle = style ?: remember(isDark) {
        HazeStyle(
            (if (isDark) Color.Black else Color.White).copy(alpha = 0.2f),
            null,
            10.dp,
            0f
        )
    }
    
    this.hazeEffect(
        state = state,
        style = resolvedStyle,
        block = block
    )
}

@Composable
fun SparkleStudyTheme(
    content: @Composable () -> Unit
) {
    val themeMode by appThemeState
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) DarkSparkleColorScheme else LightSparkleColorScheme

    // 設定 Android/iOS 系統欄顏色
    SetSystemBarsStyle(isDark = isDarkTheme)

    MaterialTheme(
        colorScheme = colorScheme
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colorScheme.onBackground,
            LocalIsDarkTheme provides isDarkTheme
        ) {
            content()
        }
    }
}
