package com.example.readingfoundations.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    appRepository: AppRepository
) : ViewModel() {

    val homeUiState: StateFlow<HomeUiState> =
        appRepository.getUserProgress().map { userProgress ->
            val progress = userProgress ?: UserProgress()
            HomeUiState(
                wordCompletionPercentage = calculateCompletionPercentage(progress.wordLevels),
                sentenceCompletionPercentage = calculateCompletionPercentage(progress.sentenceLevels)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    private fun calculateCompletionPercentage(levels: List<com.example.readingfoundations.data.models.Level>): Float {
        if (levels.isEmpty()) return 0f
        val completedLevels = levels.count { it.isCompleted }
        return (completedLevels.toFloat() / levels.size)
    }
}

data class HomeUiState(
    val wordCompletionPercentage: Float = 0f,
    val sentenceCompletionPercentage: Float = 0f
)