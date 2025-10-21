package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.PhonemeRepository
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneticsViewModel(
    private val unitRepository: UnitRepository,
    private val phonemeRepository: PhonemeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val level: Int = savedStateHandle.get<Int>("level") ?: 1
    private val _uiState = MutableStateFlow(PhoneticsUiState())
    val uiState: StateFlow<PhoneticsUiState> = _uiState.asStateFlow()

    private var practiceJob: Job? = null
    private var allPhonemes: List<Phoneme> = emptyList()
    private var levelPhonemes: List<Phoneme> = emptyList()

    init {
        viewModelScope.launch {
            allPhonemes = phonemeRepository.getAllPhonemes().first()
            levelPhonemes = allPhonemes.filter { it.level == level }
            _uiState.update { it.copy(isLoading = false, allPhonemes = allPhonemes) }
        }
    }

    fun startPractice() {
        _uiState.update { it.copy(inPracticeMode = true) }
        generateNewQuestion()
    }

    fun stopPractice() {
        practiceJob?.cancel()
        _uiState.value = PhoneticsUiState(isLoading = false, allPhonemes = allPhonemes)
    }

    fun checkAnswer(selectedOption: Phoneme) {
        val isCorrect = selectedOption.id == _uiState.value.targetPhoneme?.id
        _uiState.update { it.copy(isCorrect = isCorrect, selectedOption = selectedOption) }

        practiceJob = viewModelScope.launch {
            delay(1000) // wait for 1 second
            if (isCorrect) {
                unitRepository.updateProgress(com.example.readingfoundations.data.Subjects.PHONETICS, level)
                generateNewQuestion()
            } else {
                _uiState.update { it.copy(isCorrect = null, selectedOption = null) } // Reset for another try
            }
        }
    }

    private fun generateNewQuestion() {
        if (levelPhonemes.isEmpty()) {
            _uiState.update {
                it.copy(
                    questionPrompt = "Not enough phonemes for this level.",
                    options = emptyList()
                )
            }
            return
        }

        val targetPhoneme = levelPhonemes.random()
        // Ensure there are enough options, supplement from all phonemes if necessary
        val potentialOptions = (levelPhonemes + allPhonemes).distinctBy { it.id } - targetPhoneme
        val otherOptions = potentialOptions.shuffled().take(3)
        val options = (otherOptions + targetPhoneme).shuffled()

        val questionType = QuestionType.entries.toTypedArray().random()
        val questionPrompt = when (questionType) {
            QuestionType.SOUND_TO_GRAPHEME -> "Which letter makes the '${targetPhoneme.sound}' sound?"
            QuestionType.GRAPHEME_TO_SOUND -> "What sound does the letter '${targetPhoneme.grapheme}' make?"
            QuestionType.WORD_TO_GRAPHEME -> "What is the first letter in '${targetPhoneme.exampleWord}'?"
            QuestionType.GRAPHEME_TO_WORD -> "Which word starts with the letter '${targetPhoneme.grapheme}'?"
        }

        _uiState.update {
            it.copy(
                targetPhoneme = targetPhoneme,
                options = options,
                isCorrect = null,
                selectedOption = null,
                questionPrompt = questionPrompt,
                questionType = questionType
            )
        }
    }
}

enum class QuestionType {
    SOUND_TO_GRAPHEME, // "Which letter makes the 's' sound?" -> options are graphemes
    GRAPHEME_TO_SOUND, // "What sound does 's' make?" -> options are sounds (not implemented yet, text for now)
    WORD_TO_GRAPHEME,  // "What letter is in 'sun'?" -> options are graphemes
    GRAPHEME_TO_WORD   // "Which word starts with 's'?" -> options are example words
}

data class PhoneticsUiState(
    val isLoading: Boolean = true,
    val allPhonemes: List<Phoneme> = emptyList(),
    val inPracticeMode: Boolean = false,
    val targetPhoneme: Phoneme? = null,
    val options: List<Phoneme> = emptyList(),
    val isCorrect: Boolean? = null,
    val selectedOption: Phoneme? = null,
    val questionPrompt: String? = null,
    val questionType: QuestionType? = null
)
