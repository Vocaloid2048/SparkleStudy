package com.voc2048.sparkle_study.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.util.DebugLogger
import kotlin.math.PI

/**
 * 通用工具類，整合了時間、單位轉換、平台判斷、Coil 配置等功能。
 */
object UtilsTools {

    // --- 時間與格式化相關 (Time & Formatting) ---

    /**
     * 將秒數轉換為易讀的時間格式 (如 25:00)。
     */
    fun formatSecondsToTimerString(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }

    /**
     * 將分鐘數轉換為易讀的時間格式 (如 1h 30m 或 45m)。
     */
    fun formatMinutesToTimeString(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    /**
     * 格式化數字到指定的小數位數。
     */
    fun formatDecimal(value: Double, decimalPoints: Int): String {
        return value.toString() 
    }

    // --- 單位轉換相關 (Unit Conversion) ---

    /**
     * 將 Px 轉換為 Dp。
     */
    @Composable
    fun pxToDp(px: Float): Dp {
        return with(LocalDensity.current) { px.toDp() }
    }

    /**
     * 將 Dp 轉換為 Px。
     */
    @Composable
    fun dpToPx(dp: Dp): Float {
        return with(LocalDensity.current) { dp.toPx() }
    }

    // --- Coil 圖片載入配置 (Coil Configuration) ---

    /**
     * 創建新的 ImageLoader 實例。
     */
    fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }

    /**
     * 創建新的 ImageRequest 實例。
     */
    fun newImageRequest(context: PlatformContext, data: Any): ImageRequest {
        return ImageRequest.Builder(context)
            .data(data)
            .build()
    }

    fun toRadians(degree: Number): Double {
        return degree.toDouble() * PI / 180.0
    }
}
