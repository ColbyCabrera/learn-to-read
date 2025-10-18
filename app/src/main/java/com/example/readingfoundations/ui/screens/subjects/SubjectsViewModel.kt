package com.example.readingfoundations.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SubjectsViewModel(unitRepository: UnitRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            unitRepository.getUserProgress(),
            unitRepository.getUnits()
        ) { userProgress, units ->
            val allLevels = units.flatMap { it.levels }
            val wordLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.WORD_BUILDING }
                .map { it.levelNumber }
                .distinct()
                .count()
            val sentenceLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.SENTENCE_READING }
                .map { it.levelNumber }
                .distinct()
                .count()
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