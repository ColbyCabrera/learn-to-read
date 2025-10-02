package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneticsUiState())
    val uiState: StateFlow<PhoneticsUiState> = _uiState.asStateFlow()

    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }

    fun onLetterSelected(letter: String) {
        _uiState.update { it.copy(selectedLetter = letter) }
    }

    fun startPractice() {
        _uiState.update { it.copy(inPracticeMode = true) }
        generateNewQuestion()
    }

    fun stopPractice() {
        _uiState.value = PhoneticsUiState()
    }

    fun checkAnswer(selectedOption: String) {
        val isCorrect = selectedOption == _uiState.value.targetLetter
        _uiState.update { it.copy(isCorrect = isCorrect) }

        viewModelScope.launch {
            delay(1000) // wait for 1 second
            if (isCorrect) {
                generateNewQuestion()
            } else {
                _uiState.update { it.copy(isCorrect = null) } // Reset for another try
            }
        }
    }

    private fun generateNewQuestion() {
        val target = alphabet.random()
        val options = (alphabet - target).shuffled().take(3) + target
        _uiState.update {
            it.copy(
                targetLetter = target,
                options = options.shuffled(),
                isCorrect = null,
                selectedLetter = null
            )
        }
    }
}

data class PhoneticsUiState(
    val selectedLetter: String? = null,
    val inPracticeMode: Boolean = false,
    val targetLetter: String? = null,
    val options: List<String> = emptyList(),
    val isCorrect: Boolean? = null
)