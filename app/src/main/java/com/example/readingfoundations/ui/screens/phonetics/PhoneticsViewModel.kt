package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PhoneticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneticsUiState())
    val uiState: StateFlow<PhoneticsUiState> = _uiState

    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }

    fun onLetterSelected(letter: String) {
        // This is where the Text-to-Speech logic will be triggered.
        // For now, we'll just update the state as a placeholder.
        _uiState.value = _uiState.value.copy(selectedLetter = letter)
    }
}

data class PhoneticsUiState(
    val selectedLetter: String? = null
)