package com.voc2048.sparkle_study.utils

import com.russhwolf.settings.Settings

/**
 * 應用程式設定存儲類。
 */
class Preferences {
    private val settings: Settings = Settings()

    object Keys {
        const val APP_LANGUAGE = "app_language"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val USER_LEVEL = "user_level"
        const val USER_NAME = "user_name"
        const val USER_ID = "user_id"

        // Timer Settings (Constants/Defaults)
        const val POMODORO_FOCUS_MIN = "pomodoro_focus_min"
        const val POMODORO_BREAK_MIN = "pomodoro_break_min"
        const val POMODORO_ROUNDS = "pomodoro_rounds"
        const val COUNTDOWN_TOTAL_SECONDS = "countdown_total_seconds"
        const val TIMER_MODE = "timer_mode"
        const val TIMER_STYLE = "timer_style"
        const val TIMER_SWITCH_BEHAVIOR = "timer_switch_behavior"

        // Mode-Specific Progress (Current State)
        const val POMODORO_CURRENT_SECONDS = "pomodoro_current_seconds"
        const val POMODORO_CURRENT_PHASE = "pomodoro_current_phase"
        const val POMODORO_IS_RESTING = "pomodoro_is_resting"
        const val COUNTDOWN_CURRENT_SECONDS = "countdown_current_seconds"
        const val COUNTUP_CURRENT_SECONDS = "countup_current_seconds"

        // Global Active Session State
        const val IS_ACTIVE_SESSION_RUNNING = "is_active_session_running"
        const val LAST_ACTIVE_TIMESTAMP = "last_active_timestamp"
        const val DAILY_GOAL_MINUTES = "daily_goal_minutes"
        const val LAST_SYSTEM_TIME = "last_system_time"
        const val LAST_UPTIME = "last_uptime"
        const val DEDUCTED_COUNT_TODAY = "deducted_count_today"
        const val LAST_DEDUCTION_DATE = "last_deduction_date"
        const val LAST_LOGIN_DATE = "last_login_date"
    }

    // --- Core Identity ---
    var userId: String
        get() = settings.getString(Keys.USER_ID, "default_user")
        set(value) = settings.putString(Keys.USER_ID, value)

    var userName: String
        get() = settings.getString(Keys.USER_NAME, "Sparkle User")
        set(value) = settings.putString(Keys.USER_NAME, value)

    // --- Configuration ---
    var timerMode: String
        get() = settings.getString(Keys.TIMER_MODE, "POMODORO")
        set(value) = settings.putString(Keys.TIMER_MODE, value)

    var timerStyle: String
        get() = settings.getString(Keys.TIMER_STYLE, "SPARKLE")
        set(value) = settings.putString(Keys.TIMER_STYLE, value)

    var timerSwitchBehavior: String
        get() = settings.getString(Keys.TIMER_SWITCH_BEHAVIOR, "CONFIRM")
        set(value) = settings.putString(Keys.TIMER_SWITCH_BEHAVIOR, value)

    // --- Timer Setup Defaults ---
    var pomodoroFocusMin: Int
        get() = settings.getInt(Keys.POMODORO_FOCUS_MIN, 25)
        set(value) = settings.putInt(Keys.POMODORO_FOCUS_MIN, value)

    var pomodoroBreakMin: Int
        get() = settings.getInt(Keys.POMODORO_BREAK_MIN, 5)
        set(value) = settings.putInt(Keys.POMODORO_BREAK_MIN, value)

    var pomodoroRounds: Int
        get() = settings.getInt(Keys.POMODORO_ROUNDS, 3)
        set(value) = settings.putInt(Keys.POMODORO_ROUNDS, value)

    var countdownTotalSeconds: Int
        get() = settings.getInt(Keys.COUNTDOWN_TOTAL_SECONDS, 60 * 60)
        set(value) = settings.putInt(Keys.COUNTDOWN_TOTAL_SECONDS, value)

    // --- Real-time Progress (Per Mode) ---
    var pomodoroCurrentSeconds: Int
        get() = settings.getInt(Keys.POMODORO_CURRENT_SECONDS, 25 * 60)
        set(value) = settings.putInt(Keys.POMODORO_CURRENT_SECONDS, value)

    var pomodoroCurrentPhase: Int
        get() = settings.getInt(Keys.POMODORO_CURRENT_PHASE, 0)
        set(value) = settings.putInt(Keys.POMODORO_CURRENT_PHASE, value)

    var pomodoroIsResting: Boolean
        get() = settings.getBoolean(Keys.POMODORO_IS_RESTING, false)
        set(value) = settings.putBoolean(Keys.POMODORO_IS_RESTING, value)

    var countdownCurrentSeconds: Int
        get() = settings.getInt(Keys.COUNTDOWN_CURRENT_SECONDS, 60 * 60)
        set(value) = settings.putInt(Keys.COUNTDOWN_CURRENT_SECONDS, value)

    var countupCurrentSeconds: Int
        get() = settings.getInt(Keys.COUNTUP_CURRENT_SECONDS, 0)
        set(value) = settings.putInt(Keys.COUNTUP_CURRENT_SECONDS, value)

    // --- Runtime Sync ---
    var isActiveSessionRunning: Boolean
        get() = settings.getBoolean(Keys.IS_ACTIVE_SESSION_RUNNING, false)
        set(value) = settings.putBoolean(Keys.IS_ACTIVE_SESSION_RUNNING, value)

    var lastActiveTimestamp: Long
        get() = settings.getLong(Keys.LAST_ACTIVE_TIMESTAMP, 0L)
        set(value) = settings.putLong(Keys.LAST_ACTIVE_TIMESTAMP, value)

    // --- Misc ---
    var appLanguage: String
        get() = settings.getString(Keys.APP_LANGUAGE, "en")
        set(value) = settings.putString(Keys.APP_LANGUAGE, value)

    var notificationsEnabled: Boolean
        get() = settings.getBoolean(Keys.NOTIFICATIONS_ENABLED, true)
        set(value) = settings.putBoolean(Keys.NOTIFICATIONS_ENABLED, value)

    var dailyGoalMinutes: Int
        get() = settings.getInt(Keys.DAILY_GOAL_MINUTES, 120)
        set(value) = settings.putInt(Keys.DAILY_GOAL_MINUTES, value)

    var lastSystemTime: Long
        get() = settings.getLong(Keys.LAST_SYSTEM_TIME, 0L)
        set(value) = settings.putLong(Keys.LAST_SYSTEM_TIME, value)

    var lastUptime: Long
        get() = settings.getLong(Keys.LAST_UPTIME, 0L)
        set(value) = settings.putLong(Keys.LAST_UPTIME, value)

    var deductedCountToday: Int
        get() = settings.getInt(Keys.DEDUCTED_COUNT_TODAY, 0)
        set(value) = settings.putInt(Keys.DEDUCTED_COUNT_TODAY, value)

    var lastDeductionDate: String
        get() = settings.getString(Keys.LAST_DEDUCTION_DATE, "")
        set(value) = settings.putString(Keys.LAST_DEDUCTION_DATE, value)

    var lastLoginDate: String
        get() = settings.getString(Keys.LAST_LOGIN_DATE, "")
        set(value) = settings.putString(Keys.LAST_LOGIN_DATE, value)
}
