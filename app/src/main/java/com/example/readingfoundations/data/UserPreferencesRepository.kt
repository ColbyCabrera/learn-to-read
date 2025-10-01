package com.example.readingfoundations.data

import android.content.Context

class UserPreferencesRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    fun saveNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            putInt(KEY_HOUR, hour)
            putInt(KEY_MINUTE, minute)
            apply()
        }
    }

    fun getNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun getNotificationHour(): Int {
        return sharedPreferences.getInt(KEY_HOUR, 18)
    }

    fun getNotificationMinute(): Int {
        return sharedPreferences.getInt(KEY_MINUTE, 0)
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_HOUR = "notification_hour"
        private const val KEY_MINUTE = "notification_minute"
    }
}