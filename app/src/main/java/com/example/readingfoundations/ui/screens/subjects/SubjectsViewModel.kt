package com.example.readingfoundations.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SectionsViewModel(appRepository: AppRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            appRepository.getUserProgress(),
            appRepository.getWordLevelCount(),
            appRepository.getSentenceLevelCount()
        ) { userProgress, wordLevelCount, sentenceLevelCount ->
            HomeUiState(
                userProgress = userProgress ?: UserProgress(),
                wordLevelCount = wordLevelCount,
                sentenceLevelCount = sentenceLevelCount
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}

data class HomeUiState(
    val userProgress: UserProgress = UserProgress(),
    val wordLevelCount: Int = 0,
    val sentenceLevelCount: Int = 0
)