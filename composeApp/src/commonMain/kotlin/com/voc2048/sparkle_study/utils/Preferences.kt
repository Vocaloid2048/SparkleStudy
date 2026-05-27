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
    }

    var appLanguage: String
        get() = settings.getString(Keys.APP_LANGUAGE, "en")
        set(value) = settings.putString(Keys.APP_LANGUAGE, value)

    var notificationsEnabled: Boolean
        get() = settings.getBoolean(Keys.NOTIFICATIONS_ENABLED, true)
        set(value) = settings.putBoolean(Keys.NOTIFICATIONS_ENABLED, value)

    var userName: String
        get() = settings.getString(Keys.USER_NAME, "Sparkle User")
        set(value) = settings.putString(Keys.USER_NAME, value)
}
