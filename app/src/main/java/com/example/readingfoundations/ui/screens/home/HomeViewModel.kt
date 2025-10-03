package com.example.readingfoundations.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(appRepository: AppRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        appRepository.getUserProgress()
            .map { it ?: UserProgress() }
            .map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )
}

data class HomeUiState(
    val userProgress: UserProgress = UserProgress()
)