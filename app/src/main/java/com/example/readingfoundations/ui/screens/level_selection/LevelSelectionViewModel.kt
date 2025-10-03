package com.example.readingfoundations.ui.screens.level_selection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LevelSelectionViewModel(
    appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val category: String = checkNotNull(savedStateHandle["category"])

    val uiState: StateFlow<LevelSelectionUiState> =
        appRepository.getUserProgress().map { userProgress ->
            val progress = userProgress ?: UserProgress()
            val levels = when (category) {
                "word_building" -> progress.wordLevels
                "sentence_reading" -> progress.sentenceLevels
                else -> emptyList()
            }
            LevelSelectionUiState(levels = levels, category = category)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LevelSelectionUiState()
        )
}

data class LevelSelectionUiState(
    val levels: List<Level> = emptyList(),
    val category: String = ""
)