package com.voc2048.sparkle_study

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.KeyguardManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

private var mediaPlayer: MediaPlayer? = null

actual fun vibrate(millis: Long, pattern: LongArray?) {
    val context = platformContext.getContext() as Context
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Check system ringer mode
    if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) return

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (pattern != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(millis)
        }
    }
}

actual fun playSound(bytes: ByteArray) {
    val context = platformContext.getContext() as Context
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Respect system sound settings
    if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return

    try {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer = MediaPlayer().apply {
            setDataSource(object : android.media.MediaDataSource() {
                override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                    if (position >= bytes.size) return -1
                    val remaining = bytes.size - position
                    val length = if (size > remaining) remaining.toInt() else size
                    System.arraycopy(bytes, position.toInt(), buffer, offset, length)
                    return length
                }

                override fun getSize(): Long = bytes.size.toLong()
                override fun close() {}
            })
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            prepare()
            start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private const val CHANNEL_ID = "timer_notifications"

actual fun showNotification(title: String, content: String) {
    val context = platformContext.getContext() as Context
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "專注通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "計時器結束與提醒通知"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val activity = platformContext.getActivity() as ComponentActivity
    val intent = Intent(context, activity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) com.voc2048.sparkle_study.R.drawable.app_icon_monet else com.voc2048.sparkle_study.R.drawable.app_icon_round)
        .setColor(0xFF59B292.toInt())
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // 檢查通知權限 (Android 13+)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        notificationManager.notify(1, notification)
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
    val context = platformContext.getContext() as Context
    val intent = Intent(context, TimerService::class.java).apply {
        putExtra(TimerService.EXTRA_TIME_LEFT, timeLeft)
        putExtra(TimerService.EXTRA_TOTAL_TIME, totalTime)
        putExtra(TimerService.EXTRA_MODE, mode)
        putExtra(TimerService.EXTRA_PHASE, phase)
        putExtra(TimerService.EXTRA_IS_RESTING, isResting)
        putExtra(TimerService.EXTRA_IS_RUNNING, isRunning)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

actual fun stopTimerService() {
    val context = platformContext.getContext() as Context
    val intent = Intent(context, TimerService::class.java).apply {
        action = TimerService.ACTION_STOP
    }
    context.startService(intent)
}

actual fun isUserDistracted(): Boolean {
    val context = platformContext.getContext() as Context
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    
    val isScreenOff = !powerManager.isInteractive
    val isLocked = keyguardManager.isKeyguardLocked
    
    return isScreenOff || isLocked
}

actual fun getUptimeMillis(): Long = android.os.SystemClock.elapsedRealtime()

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

@Composable
actual fun SetSystemBarsStyle(isDark: Boolean) {
    val view = LocalView.current
    val context = view.context
    if (context is ComponentActivity) {
        SideEffect {
            context.enableEdgeToEdge(
                statusBarStyle = if (isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                },
                navigationBarStyle = if (isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
            )
        }
    }
}
