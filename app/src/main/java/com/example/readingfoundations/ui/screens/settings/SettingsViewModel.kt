package com.example.readingfoundations.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.readingfoundations.data.UserPreferencesRepository
import com.example.readingfoundations.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            notificationsEnabled = userPreferencesRepository.getNotificationEnabled(),
            notificationHour = userPreferencesRepository.getNotificationHour(),
            notificationMinute = userPreferencesRepository.getNotificationMinute()
        )
    }

    fun onNotificationToggled(enabled: Boolean, context: Context) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        saveSettings()
        if (enabled) {
            NotificationScheduler.scheduleNotification(
                context,
                _uiState.value.notificationHour,
                _uiState.value.notificationMinute
            )
        } else {
            NotificationScheduler.cancelNotification(context)
        }
    }

    fun onTimeSelected(hour: Int, minute: Int, context: Context) {
        _uiState.value = _uiState.value.copy(
            notificationHour = hour,
            notificationMinute = minute
        )
        saveSettings()
        if (_uiState.value.notificationsEnabled) {
            NotificationScheduler.scheduleNotification(context, hour, minute)
        }
    }

    private fun saveSettings() {
        userPreferencesRepository.saveNotificationSettings(
            enabled = _uiState.value.notificationsEnabled,
            hour = _uiState.value.notificationHour,
            minute = _uiState.value.notificationMinute
        )
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 18,
    val notificationMinute: Int = 0
)