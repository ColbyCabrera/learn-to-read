package com.example.readingfoundations.ui.screens.phonetics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
            .map { userProgress: UserProgress? ->
                userProgress?.completedLevels?.get(Subjects.PHONETICS)?.associateWith { 100 } ?: emptyMap()
            }
            .distinctUntilChanged()
            .onEach { progressMap ->
                _uiState.value = _uiState.value.copy(progressMap = progressMap)
            }
            .catch { e ->
                Log.e("PhoneticsLevelsViewModel", "Error observing user progress", e)
                // Optionally update UI to show an error or a default state
                 _uiState.value = _uiState.value.copy(progressMap = emptyMap())
            }
            .launchIn(viewModelScope)
    }
}

data class PhoneticsLevelsUiState(
    val levelCount: Int = 0,
    val progressMap: Map<Int, Int> = emptyMap()
)
