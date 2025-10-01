package com.example.readingfoundations.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.readingfoundations.data.UserPreferencesRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val userPreferencesRepository = UserPreferencesRepository(context)
            val notificationsEnabled = userPreferencesRepository.getNotificationEnabled()
            if (notificationsEnabled) {
                val hour = userPreferencesRepository.getNotificationHour()
                val minute = userPreferencesRepository.getNotificationMinute()
                NotificationScheduler.scheduleNotification(context, hour, minute)
            }
        }
    }
}