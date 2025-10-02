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

    val phonemes = listOf(
        Phoneme("a", "apple", "a as in apple"),
        Phoneme("b", "ball", "b as in ball"),
        Phoneme("c", "cat", "c as in cat"),
        Phoneme("d", "dog", "d as in dog"),
        Phoneme("e", "egg", "e as in egg"),
        Phoneme("f", "fish", "f as in fish"),
        Phoneme("g", "goat", "g as in goat"),
        Phoneme("h", "hat", "h as in hat"),
        Phoneme("i", "igloo", "i as in igloo"),
        Phoneme("j", "jam", "j as in jam"),
        Phoneme("k", "kite", "k as in kite"),
        Phoneme("l", "lion", "l as in lion"),
        Phoneme("m", "monkey", "m as in monkey"),
        Phoneme("n", "nest", "n as in nest"),
        Phoneme("o", "octopus", "o as in octopus"),
        Phoneme("p", "pig", "p as in pig"),
        Phoneme("q", "queen", "q as in queen"),
        Phoneme("r", "rabbit", "r as in rabbit"),
        Phoneme("s", "sun", "s as in sun"),
        Phoneme("t", "turtle", "t as in turtle"),
        Phoneme("u", "umbrella", "u as in umbrella"),
        Phoneme("v", "violin", "v as in violin"),
        Phoneme("w", "whale", "w as in whale"),
        Phoneme("x", "box", "x as in box"),
        Phoneme("y", "yo-yo", "y as in yo-yo"),
        Phoneme("z", "zebra", "z as in zebra")
    )

    fun onLetterSelected(phoneme: Phoneme) {
        _uiState.update { it.copy(selectedPhoneme = phoneme) }
    }

    fun startPractice() {
        _uiState.update { it.copy(inPracticeMode = true) }
        generateNewQuestion()
    }

    fun stopPractice() {
        _uiState.value = PhoneticsUiState()
    }

    fun checkAnswer(selectedOption: Phoneme) {
        val isCorrect = selectedOption == _uiState.value.targetPhoneme
        _uiState.update { it.copy(isCorrect = isCorrect, selectedPhoneme = selectedOption) }

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
        val target = phonemes.random()
        val options = (phonemes - target).shuffled().take(3) + target
        _uiState.update {
            it.copy(
                targetPhoneme = target,
                options = options.shuffled(),
                isCorrect = null,
                selectedPhoneme = null
            )
        }
    }
}

data class PhoneticsUiState(
    val selectedPhoneme: Phoneme? = null,
    val inPracticeMode: Boolean = false,
    val targetPhoneme: Phoneme? = null,
    val options: List<Phoneme> = emptyList(),
    val isCorrect: Boolean? = null
)

data class Phoneme(
    val sound: String,
    val exampleWord: String,
    val ttsText: String
)