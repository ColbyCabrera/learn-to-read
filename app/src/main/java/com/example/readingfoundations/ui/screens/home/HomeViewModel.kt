package com.example.readingfoundations.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Unit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val unitRepository: UnitRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            unitRepository.getUnits(),
            unitRepository.getUserProgress()
        ) { units, userProgress ->
            HomeUiState(units, userProgress ?: UserProgress())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun updateProgress(subject: String, level: Int) {
        viewModelScope.launch {
            unitRepository.updateProgress(subject, level)
        }
    }
}

data class HomeUiState(
    val units: List<Unit> = emptyList(),
    val userProgress: com.example.readingfoundations.data.models.UserProgress = com.example.readingfoundations.data.models.UserProgress()
)