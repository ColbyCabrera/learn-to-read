package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.PhoneticsData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.readingfoundations.data.UnitRepository

class PhoneticsViewModel(private val unitRepository: UnitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneticsUiState())
    val uiState: StateFlow<PhoneticsUiState> = _uiState.asStateFlow()

    private var practiceJob: Job? = null

    fun onLetterSelected(letter: String) {
        _uiState.update { it.copy(selectedLetter = letter) }
    }

    fun startPractice() {
        _uiState.update { it.copy(inPracticeMode = true) }
        generateNewQuestion()
    }

    fun stopPractice() {
        practiceJob?.cancel()
        _uiState.value = PhoneticsUiState()
    }

    /**
     * Records the user's selected option for the current question and advances practice state.
     *
     * Updates UI state with the chosen option and whether it matches the current target. After a short delay,
     * if the selection is correct the user's phonetics progress is incremented and a new question is generated;
     * if incorrect the correctness state is cleared to allow another attempt.
     *
     * @param selectedOption The option the user selected (a grapheme or example word depending on the question).
     */
    fun checkAnswer(selectedOption: String) {
        val isCorrect = selectedOption == _uiState.value.targetLetter
        _uiState.update { it.copy(isCorrect = isCorrect, selectedLetter = selectedOption) }

        practiceJob = viewModelScope.launch {
            delay(1000) // wait for 1 second
            if (isCorrect) {
                unitRepository.updateProgress("Phonetics", 1)
                generateNewQuestion()
            } else {
                _uiState.update { it.copy(isCorrect = null) } // Reset for another try
            }
        }
    }

    private fun generateNewQuestion() {
        val targetPhoneme = PhoneticsData.phonemes.random()
        val otherPhonemes = (PhoneticsData.phonemes - targetPhoneme).shuffled().take(3)
        val options = (otherPhonemes + targetPhoneme)

        val questionType = QuestionType.entries.toTypedArray().random()
        val questionPrompt = when (questionType) {
            QuestionType.FIRST_SOUND -> "What letter makes the first sound in '${targetPhoneme.exampleWord}'?"
            QuestionType.WHICH_LETTER -> "Which letter is for '${targetPhoneme.exampleWord}'?"
            QuestionType.WHAT_IS_THE_LETTER -> "The word is '${targetPhoneme.exampleWord}'. What is the first letter?"
            QuestionType.IPA_SYMBOL -> "Which letter makes the sound at the beginning of '${targetPhoneme.exampleWord}'?"
            QuestionType.WHICH_WORD -> {
                val soundToSay = PhoneticsData.phoneticPronunciations[targetPhoneme.grapheme]
                "Which word starts with the '${soundToSay}' sound?"
            }
        }

        if (questionType == QuestionType.WHICH_WORD) {
             _uiState.update { it ->
                 it.copy(
                    targetLetter = targetPhoneme.exampleWord,
                    options = options.map { it.exampleWord }.shuffled(),
                    isCorrect = null,
                    selectedLetter = null,
                    questionPrompt = questionPrompt
                )
            }
        } else {
            _uiState.update { it ->
                it.copy(
                    targetLetter = targetPhoneme.grapheme,
                    options = options.map { it.grapheme }.shuffled(),
                    isCorrect = null,
                    selectedLetter = null,
                    questionPrompt = questionPrompt
                )
            }
        }
    }
}

enum class QuestionType {
    FIRST_SOUND,
    WHICH_LETTER,
    WHAT_IS_THE_LETTER,
    IPA_SYMBOL,
    WHICH_WORD
}

data class PhoneticsUiState(
    val selectedLetter: String? = null,
    val inPracticeMode: Boolean = false,
    val targetLetter: String? = null,
    val options: List<String> = emptyList(),
    val isCorrect: Boolean? = null,
    val questionPrompt: String? = null
)