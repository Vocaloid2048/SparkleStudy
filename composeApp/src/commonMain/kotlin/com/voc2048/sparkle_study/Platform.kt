package com.voc2048.sparkle_study
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import com.voc2048.sparkle_study.types.DeviceInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.utils.io.ByteReadChannel

/**
 * This is the declaration kt file for specific-platform function
 * THIS IS COMMON-MAIN, so ONLY EXPECT
 */

expect fun getImageBitmapByByteArray(byteArray: ByteArray): ImageBitmap;

expect fun getByteArrayByImageBitmap(imageBitmap: ImageBitmap): ByteArray;

@Composable
expect fun getIsLandscape(): Boolean;

/** Getting screen size info for UI-related calculations */
data class ScreenSizeInfo(val hPX: Int, val wPX: Int, val hDP: Dp, val wDP: Dp)

@Composable
@Deprecated("Use BoxWithConstraints() instead")
expect fun getScreenSizeInfo(): ScreenSizeInfo

expect fun getDeviceInfo(): DeviceInfo

expect fun getLocalHttpClient(function: HttpClientConfig<*>.() -> Unit): HttpClient

expect fun changeLanguage(language: String, region: String? = null)

//ref: https://medium.com/@robert.jamison/passing-android-context-in-kmp-jetpack-compose-8de5b5de7bdd
expect class ContextFactory {
    fun getContext(): Any
    fun getApplication(): Any
    fun getActivity(): Any
}

expect fun getAppSpecificDirectory(): okio.Path

//ref: https://stackoverflow.com/questions/78739232/how-to-save-a-response-body-to-a-file-in-kotlin-multiplatform-with-ktor
expect suspend fun ByteReadChannel.writeToFile(filepath: String)

expect fun exitApp(): Unit

// --- New Platform Utilities ---

/**
 * Vibrate the device.
 * @param millis Duration in milliseconds (for simple vibration)
 * @param pattern Optional list of (duration, pause) pairs in milliseconds for custom patterns
 */
expect fun vibrate(millis: Long, pattern: LongArray? = null)

/**
 * Play a sound using raw bytes.
 */
expect fun playSound(bytes: ByteArray)

/**
 * Show a local notification.
 * @param title Notification title
 * @param content Notification content
 */
expect fun showNotification(title: String, content: String)

/**
 * Start or update the foreground timer service.
 */
expect fun updateTimerService(
    timeLeft: Int,
    totalTime: Int,
    mode: String,
    phase: Int,
    isResting: Boolean,
    isRunning: Boolean
)

/**
 * Stop the foreground timer service.
 */
expect fun stopTimerService()

/**
 * Check if the user is currently distracted (screen off or device locked).
 */
expect fun isUserDistracted(): Boolean
