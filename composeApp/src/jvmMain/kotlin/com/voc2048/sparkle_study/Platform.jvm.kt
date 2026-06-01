package com.voc2048.sparkle_study


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import com.voc2048.sparkle_study.types.DeviceInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyTo
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import java.awt.*
import java.io.File
import java.util.Locale
import javax.sound.sampled.AudioSystem
import kotlin.system.exitProcess

/**
 * This is the declaration kt file for specific-platform function
 * THIS IS JVM-MAIN, so ONLY ACTUAL
 */

actual fun vibrate(millis: Long, pattern: LongArray?) {
    // Desktop usually doesn't have vibration. Fallback to beep.
    Toolkit.getDefaultToolkit().beep()
}

actual fun playSound(bytes: ByteArray) {
    try {
        val inputStream = java.io.ByteArrayInputStream(bytes)
        val player = javazoom.jl.player.Player(inputStream)
        
        // Run in a separate thread to avoid blocking the main/UI thread
        Thread {
            try {
                player.play()
            } catch (e: Exception) {
                println("Error during MP3 playback: ${e.message}")
            } finally {
                player.close()
            }
        }.start()
    } catch (e: Exception) {
        println("Error initializing MP3 player: ${e.message}")
    }
}

actual fun showNotification(title: String, content: String) {
    if (!SystemTray.isSupported()) return
    
    try {
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage("") // Empty image for icon
        val trayIcon = TrayIcon(image, "Sparkle Study")
        trayIcon.isImageAutoSize = true
        tray.add(trayIcon)
        trayIcon.displayMessage(title, content, TrayIcon.MessageType.INFO)
        
        // Remove icon after a delay
        Thread {
            Thread.sleep(5000)
            tray.remove(trayIcon)
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual fun updateTimerService(
    timeLeft: Int,
    totalTime: Int,
    mode: String,
    phase: Int,
    isResting: Boolean,
    isRunning: Boolean
) {
    // Not implemented for JVM
}

actual fun stopTimerService() {
    // Not implemented for JVM
}

actual fun isUserDistracted(): Boolean = false

actual fun getUptimeMillis(): Long = System.nanoTime() / 1_000_000

actual fun getImageBitmapByByteArray(byteArray: ByteArray): ImageBitmap {
    val skiaImage = Image.makeFromEncoded(byteArray)
    return skiaImage.toComposeImageBitmap()
}

actual fun getByteArrayByImageBitmap(imageBitmap: ImageBitmap): ByteArray {
    val skiaImage = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
    return skiaImage.encodeToData()!!.bytes
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getIsLandscape(): Boolean {
    val windowInfo = LocalWindowInfo.current
    return windowInfo.containerSize.width > windowInfo.containerSize.height
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Deprecated("Please use BoxWithConstraints instead")
actual fun getScreenSizeInfo(): ScreenSizeInfo {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    return remember(density, windowInfo) {
        ScreenSizeInfo(
            hPX = windowInfo.containerSize.height,
            wPX = windowInfo.containerSize.width,
            hDP = with(density) { windowInfo.containerSize.height.toDp() },
            wDP = with(density) { windowInfo.containerSize.width.toDp() }
        )
    }
}

actual fun getDeviceInfo(): DeviceInfo {
    //Return a DeviceInfo object that contains suitable OS version, device name data
    return DeviceInfo("Unspecified", System.getProperty("os.name"), System.getProperty("os.version"))
}

actual fun getLocalHttpClient(function: HttpClientConfig<*>.() -> Unit): HttpClient{
    return HttpClient(engineFactory = CIO, block = function)
}

actual fun changeLanguage(language: String, region : String?) {
    val locale = if(region == null) Locale(language) else Locale(language, region)
    Locale.setDefault(locale)
}

actual fun getAppSpecificDirectory(): Path {
    val userHome = System.getProperty("user.home")
    return "$userHome/.Stargazer3".toPath()
}

//Desktop will never use this
actual class ContextFactory {
    actual fun getContext(): Any {
        return false
    }

    actual fun getApplication(): Any {
        return false
    }

    actual fun getActivity(): Any {
        return false
    }
}

actual suspend fun ByteReadChannel.writeToFile(filepath: String) {
    this.copyTo(File(filepath).writeChannel())
}

actual fun exitApp() {
    exitProcess(0)
}
