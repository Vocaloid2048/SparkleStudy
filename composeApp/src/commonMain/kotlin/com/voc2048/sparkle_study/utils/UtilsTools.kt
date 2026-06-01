package com.voc2048.sparkle_study.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.voc2048.sparkle_study.getLocalHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI

/**
 * 通用工具類，整合了時間、單位轉換、平台判斷、Coil 配置等功能。
 */
object UtilsTools {
    
    /**
     * 從網路獲取真實的 Unix 時間戳 (UTC 毫秒)。
     * 使用 TimeAPI.io 作為來源。
     */
    suspend fun getNetworkTime(): Long? {
        return try {
            val client = getLocalHttpClient { }
            // 使用 timeapi.io 獲取台北時間
            val response = client.get("https://timeapi.io/api/time/current/zone?timeZone=Asia/Hong_Kong")
            val jsonBody = response.bodyAsText()
            val json = Json.parseToJsonElement(jsonBody).jsonObject
            
            // 解析 dateTime 字串 (例如: "2023-10-25T14:30:45.1234567")
            val dateTimeStr = json["dateTime"]?.jsonPrimitive?.content ?: return null
            
            // 轉換為 Instant 並獲取毫秒數 (指定時區為 Asia/Hong_Kong)
            val localDateTime = LocalDateTime.parse(dateTimeStr)
            localDateTime.toInstant(TimeZone.of("Asia/Hong_Kong")).toEpochMilliseconds()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- 時間與格式化相關 (Time & Formatting) ---

    /**
     * 將秒數轉換為易讀的時間格式 (如 00:25:00)。
     */
    fun formatSecondsToTimerString(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
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
