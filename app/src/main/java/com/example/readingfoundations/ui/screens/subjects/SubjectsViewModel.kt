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
                .maxOfOrNull { it } ?: 0
            val sentenceLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.SENTENCE_READING }
                .map { it.levelNumber }
                .maxOfOrNull { it } ?: 0
            val phoneticsLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.PHONETICS }
                .map { it.levelNumber }
                .maxOfOrNull { it } ?: 0
            val punctuationLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.PUNCTUATION }
                .map { it.levelNumber }
                .maxOfOrNull { it } ?: 0
            val readingComprehensionLevelCount = allLevels.asSequence()
                .filter { it.subject == Subjects.READING_COMPREHENSION }
                .map { it.levelNumber }
                .maxOfOrNull { it } ?: 0

            HomeUiState(
                userProgress = userProgress ?: UserProgress(),
                wordLevelCount = wordLevelCount,
                sentenceLevelCount = sentenceLevelCount,
                phoneticsLevelCount = phoneticsLevelCount,
                punctuationLevelCount = punctuationLevelCount,
                readingComprehensionLevelCount = readingComprehensionLevelCount
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
    val sentenceLevelCount: Int = 0,
    val phoneticsLevelCount: Int = 0,
    val punctuationLevelCount: Int = 0,
    val readingComprehensionLevelCount: Int = 0
)
