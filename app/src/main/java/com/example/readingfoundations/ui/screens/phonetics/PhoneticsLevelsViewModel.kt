package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PhoneticsLevelsViewModel(private val unitRepository: UnitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneticsLevelsUiState())
    val uiState: StateFlow<PhoneticsLevelsUiState> = _uiState.asStateFlow()

    init {
        loadLevelCount()
        observeUserProgress()
    }

    private fun loadLevelCount() {
        viewModelScope.launch {
            // Hardcoding for now, will fetch from repository later
            _uiState.value = _uiState.value.copy(levelCount = 5)
        }
    }

    private fun observeUserProgress() {
        unitRepository.getUserProgress()
            .onEach { userProgress: UserProgress? ->
                val progressMap = userProgress?.completedLevels?.get(Subjects.PHONETICS)?.associateWith { 100 } ?: emptyMap()
                _uiState.value = _uiState.value.copy(progressMap = progressMap)
            }
            .launchIn(viewModelScope)
    }
}

data class PhoneticsLevelsUiState(
    val levelCount: Int = 0,
    val progressMap: Map<Int, Int> = emptyMap()
)
