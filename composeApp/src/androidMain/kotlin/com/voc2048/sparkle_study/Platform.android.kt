package com.voc2048.sparkle_study

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.voc2048.sparkle_study.types.DeviceInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyTo
import okio.Path
import okio.Path.Companion.toPath
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

/**
 * This is the declaration kt file for specific-platform function
 * THIS IS ANDROID-MAIN, so ONLY ACTUAL
 */



actual fun getImageBitmapByByteArray(byteArray: ByteArray): ImageBitmap {
    val bitmap : ImageBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size).asImageBitmap()
    bitmap.prepareToDraw()
    return bitmap;
}

actual fun getByteArrayByImageBitmap(imageBitmap: ImageBitmap): ByteArray {
    // 將 Compose 的 ImageBitmap 轉為 Android 的 Bitmap
    val androidBitmap: Bitmap = imageBitmap.asAndroidBitmap()

    // 使用 ByteArrayOutputStream 將 Bitmap 壓縮為 PNG 格式的 ByteArray
    val outputStream = ByteArrayOutputStream()
    androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

    // 返回 ByteArray
    return outputStream.toByteArray()
}

@Composable
actual fun getIsLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
actual fun getScreenSizeInfo(): ScreenSizeInfo {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val hDp = config.screenHeightDp.dp
    val wDp = config.screenWidthDp.dp

    return ScreenSizeInfo(
        hPX = with(density) { hDp.roundToPx() },
        wPX = with(density) { wDp.roundToPx() },
        hDP = hDp,
        wDP = wDp
    )
}

actual fun getDeviceInfo(): DeviceInfo = DeviceInfo(
    deviceModel = Build.MODEL,
    deviceOSName = "Android",
    deviceOSVersion = Build.VERSION.SDK_INT.toString()
)

actual fun getLocalHttpClient(function: HttpClientConfig<*>.() -> Unit): HttpClient{
    return HttpClient(engineFactory = OkHttp, block = function)
}

actual fun changeLanguage(language: String, region: String?) {
    val locale = if(region == null) Locale(language) else Locale(language, region)
    Locale.setDefault(locale)
}

actual fun getAppSpecificDirectory(): Path {
    val context: Context = platformContext.getContext() as Context
    return context.filesDir.absolutePath.toPath()
}

actual class ContextFactory(private val activity: ComponentActivity) {
    actual fun getContext(): Any = activity.baseContext
    actual fun getApplication(): Any = activity.application
    actual fun getActivity(): Any = activity
}

actual suspend fun ByteReadChannel.writeToFile(filepath: String) {
    this.copyTo(File(filepath).writeChannel())
}

actual fun exitApp() {
    exitProcess(0)
}
