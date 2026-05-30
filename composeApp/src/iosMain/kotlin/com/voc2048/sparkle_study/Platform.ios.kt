package com.voc2048.sparkle_study

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import okio.Buffer
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.Timeout
import okio.buffer
import okio.use
import org.jetbrains.skia.Image
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAudioPlayer
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.system.exitProcess

/**
 * This is the declaration kt file for specific-platform function
 * THIS IS NATIVE-MAIN, so ONLY ACTUAL (iOS)
 */

private var iosPlayer: AVAudioPlayer? = null

actual fun vibrate(millis: Long, pattern: LongArray?) {
    // iOS simple vibration. Custom pattern is hard on iOS without CoreHaptics.
    // Taptic Engine (UIImpactFeedbackGenerator) is better for modern iOS
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun playSound(bytes: ByteArray) {
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    iosPlayer = AVAudioPlayer(data = nsData, error = null)
    iosPlayer?.play()
}

actual fun showNotification(title: String, content: String) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, error ->
        if (granted) {
            val notificationContent = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(content)
            }
            // 立即發送 (1秒後)
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
            val request = UNNotificationRequest.requestWithIdentifier("timer_notification", notificationContent, trigger)
            center.addNotificationRequest(request, null)
        }
    }
}

actual fun getImageBitmapByByteArray(byteArray: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}

actual fun getByteArrayByImageBitmap(imageBitmap: ImageBitmap): ByteArray {
    return Image.makeFromBitmap(imageBitmap.asSkiaBitmap()).encodeToData()!!.bytes
}

@Composable
actual fun getIsLandscape(): Boolean {
    val orientation = UIDevice.currentDevice.orientation.value
    return orientation == UIInterfaceOrientationLandscapeLeft || orientation == UIInterfaceOrientationLandscapeRight

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSizeInfo(): ScreenSizeInfo {
    val density = LocalDensity.current
    val config = LocalWindowInfo.current.containerSize


    return ScreenSizeInfo(
        hPX = config.height,
        wPX = config.width,
        hDP = with(density) { config.height.toDp() },
        wDP = with(density) { config.width.toDp() }
    )
}

actual fun getLocalHttpClient(function: HttpClientConfig<*>.() -> Unit): HttpClient{
    return HttpClient(engineFactory = Darwin, block = function)
}

actual fun changeLanguage(language: String, region : String?) {
    val lang = if(region != null && language.lowercase() == "zh") {
        "$language-$region"
    }else {
        language
    }
    NSUserDefaults.standardUserDefaults.setObject(arrayListOf(lang),"AppleLanguages")
}


//iOS will never use this
actual class ContextFactory {
    // Bundle allows you to lookup resources
    actual fun getContext(): Any = NSBundle
    // UIApplication allows you to access all app info
    actual fun getApplication(): Any = UIApplication
    // RootViewController can be used to identify your current screen
    actual fun getActivity(): Any = UIApplication.sharedApplication.keyWindow?.rootViewController ?: ""
}

actual fun getAppSpecificDirectory(): okio.Path {
    val fileManager = NSFileManager.defaultManager()
    val urls = fileManager.URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask)
    val appSupportDir = urls.last() as NSURL
    return appSupportDir.path?.toPath() ?: throw IllegalStateException("Could not get the path for app support directory")
}

private const val BUFFER_SIZE = 4096

class ByteReadChannelSource(
    private val channel: ByteReadChannel,
    private val scope: CoroutineScope = GlobalScope
) : Source {
    override fun read(sink: Buffer, byteCount: Long): Long {
        if (channel.isClosedForRead) return -1L

        return runBlocking(scope.coroutineContext) {
            val buffer = ByteArray(byteCount.coerceAtMost(8192).toInt())
            val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
            if (bytesRead < 0) {
                -1L
            } else {
                sink.write(buffer, 0, bytesRead)
                bytesRead.toLong()
            }
        }
    }

    override fun close() {
        channel.cancel()
    }

    override fun timeout(): Timeout = Timeout.NONE
}

// 擴展函數
fun ByteReadChannel.copyToOkio(sink: Sink, limit: Long = Long.MAX_VALUE): Long {
    val source = ByteReadChannelSource(this)
    return source.use { okioSource ->
        val bufferedSink = sink.buffer()
        var totalBytesCopied = 0L
        bufferedSink.use {
            while (totalBytesCopied < limit) {
                val bytesRead = okioSource.read(bufferedSink.buffer, minOf(limit - totalBytesCopied, 8192L))
                if (bytesRead == -1L) break
                totalBytesCopied += bytesRead
            }
            totalBytesCopied
        }
    }
}

actual suspend fun ByteReadChannel.writeToFile(filepath: String) {
    val path = filepath.toPath()
    val parent = path.parent
    if (parent != null && !FileSystem.SYSTEM.exists(parent)) {
        FileSystem.SYSTEM.createDirectories(parent)
    }
    this.copyToOkio(FileSystem.SYSTEM.sink(filepath.toPath()))
}

actual fun exitApp() {
    exitProcess(0)
}