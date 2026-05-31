package com.voc2048.sparkle_study

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_STOP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                    return START_NOT_STICKY
                }
                ACTION_PAUSE -> TimerEventBus.post(TimerEvent.PAUSE)
                ACTION_RESUME -> TimerEventBus.post(TimerEvent.RESUME)
                ACTION_SKIP -> TimerEventBus.post(TimerEvent.SKIP)
            }
        }

        val timeLeft = intent?.getIntExtra(EXTRA_TIME_LEFT, 0) ?: 0
        val totalTime = intent?.getIntExtra(EXTRA_TOTAL_TIME, 0) ?: 0
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: ""
        val phase = intent?.getIntExtra(EXTRA_PHASE, 0) ?: 0
        val isResting = intent?.getBooleanExtra(EXTRA_IS_RESTING, false) ?: false
        val isRunning = intent?.getBooleanExtra(EXTRA_IS_RUNNING, false) ?: false

        showNotification(timeLeft, totalTime, mode, phase, isResting, isRunning)

        return START_STICKY
    }

    private fun showNotification(
        timeLeft: Int,
        totalTime: Int,
        mode: String,
        phase: Int,
        isResting: Boolean,
        isRunning: Boolean
    ) {
        val channelId = "timer_ongoing"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "計時器進度",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val h = timeLeft / 3600
        val m = (timeLeft % 3600) / 60
        val s = timeLeft % 60
        val timeStr = if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
        val finalTimeStr = if (isRunning) timeStr else "$timeStr (已暫停)"

        val modeName = when (mode) {
            "POMODORO" -> if (isResting) "休息中" else "番茄鐘"
            "COUNT_UP" -> "正計時"
            "COUNT_DOWN" -> "倒計時"
            else -> mode
        }

        val roundInfo = if (mode == "POMODORO") " (第 ${phase / 2 + 1} 輪)" else ""
        
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) com.voc2048.sparkle_study.R.drawable.app_icon_monet else com.voc2048.sparkle_study.R.drawable.app_icon_round)
            .setColor(0xFF59B292.toInt())
            .setContentTitle("$modeName$roundInfo")
            .setContentText("剩餘時間: $finalTimeStr")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        // Actions
        if (isRunning) {
            val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
            val pPause = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_pause, "暫停", pPause)
        } else {
            val resumeIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESUME }
            val pResume = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "繼續", pResume)
        }

        if (mode == "POMODORO") {
            val skipIntent = Intent(this, TimerService::class.java).apply { action = ACTION_SKIP }
            val pSkip = PendingIntent.getService(this, 3, skipIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_next, "跳過", pSkip)
        }

        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_TIME_LEFT = "EXTRA_TIME_LEFT"
        const val EXTRA_TOTAL_TIME = "EXTRA_TOTAL_TIME"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val EXTRA_PHASE = "EXTRA_PHASE"
        const val EXTRA_IS_RESTING = "EXTRA_IS_RESTING"
        const val EXTRA_IS_RUNNING = "EXTRA_IS_RUNNING"
    }
}
