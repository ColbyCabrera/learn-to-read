package com.example.readingfoundations.ui.screens.phonetics

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.PhonemeRepository
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var allPhonemes: List<Phoneme> = emptyList()
    private var levelPhonemes: List<Phoneme> = emptyList()

    init {
        viewModelScope.launch {
            allPhonemes = phonemeRepository.getAllPhonemes().first()
            levelPhonemes = allPhonemes.filter { it.level == level }
            _uiState.update { it.copy(isLoading = false, phonemes = levelPhonemes, currentLevel = level) }
        }
    }

    fun startPractice() {
        val quizState = QuizState(
            questions = levelPhonemes.shuffled(),
            currentQuestionIndex = 0,
            score = 0
        )
        _uiState.update { it.copy(isPracticeMode = true, quizState = quizState) }
        generateNewQuestion()
    }

    fun checkAnswer(selectedOption: Phoneme) {
        val quizState = _uiState.value.quizState ?: return
        val isCorrect = selectedOption.id == quizState.targetPhoneme?.id
        val newScore = if (isCorrect) quizState.score + 1 else quizState.score

        _uiState.update {
            it.copy(
                quizState = quizState.copy(
                    isAnswerCorrect = isCorrect,
                    selectedOption = selectedOption,
                    score = newScore
                )
            )
        }
    }

    fun nextQuestion() {
        val quizState = _uiState.value.quizState ?: return
        if (quizState.currentQuestionIndex < quizState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    quizState = quizState.copy(
                        currentQuestionIndex = quizState.currentQuestionIndex + 1,
                        isAnswerCorrect = null,
                        selectedOption = null
                    )
                )
            }
            generateNewQuestion()
        } else {
            // Quiz finished
            viewModelScope.launch {
                val score = quizState.score
                try {
                    unitRepository.updateProgress(com.example.readingfoundations.data.Subjects.PHONETICS, level)
                } catch (e: Exception) {
                    Log.e("PhoneticsViewModel", "Failed to update progress", e)
                }
                _navigationEvent.send(
                    NavigationEvent.LevelComplete(
                        level = level,
                        score = score,
                        totalQuestions = quizState.questions.size
                    )
                )
            }
        }
    }

    private fun generateNewQuestion() {
        val quizState = _uiState.value.quizState ?: return
        if (levelPhonemes.isEmpty()) {
            _uiState.update {
                it.copy(
                    quizState = quizState.copy(
                        questionPrompt = "Not enough phonemes for this level.",
                        options = emptyList()
                    )
                )
            }
            return
        }

        val questionType = QuestionType.entries.toTypedArray().random()
        val targetPhoneme: Phoneme = quizState.questions[quizState.currentQuestionIndex]

        val getDisplayLabel: (Phoneme, QuestionType) -> String = { phoneme, type ->
            when (type) {
                QuestionType.GRAPHEME_TO_WORD -> phoneme.exampleWord
                QuestionType.GRAPHEME_TO_SOUND -> phoneme.sound
                else -> phoneme.grapheme
            }
        }

        val potentialOptions = (levelPhonemes + allPhonemes).distinctBy { it.id } - targetPhoneme
        val otherOptions = mutableListOf<Phoneme>()
        val usedLabels = mutableSetOf(getDisplayLabel(targetPhoneme, questionType))

        for (phoneme in potentialOptions.shuffled()) {
            if (otherOptions.size >= 3) break
            val label = getDisplayLabel(phoneme, questionType)
            if (label !in usedLabels) {
                otherOptions.add(phoneme)
                usedLabels.add(label)
            }
        }
        
        if (otherOptions.size < 3) {
            val remainingOptions = potentialOptions.filter { it !in otherOptions }
            for (phoneme in remainingOptions.shuffled()) {
                if (otherOptions.size >= 3) break
                otherOptions.add(phoneme)
            }
        }
        
        val options = (otherOptions + targetPhoneme).shuffled()


        val questionPrompt = when (questionType) {
            QuestionType.SOUND_TO_GRAPHEME -> "Which grapheme makes the '${targetPhoneme.sound}' sound?"
            QuestionType.GRAPHEME_TO_SOUND -> "What sound does the grapheme '${targetPhoneme.grapheme}' make?"
            QuestionType.WORD_TO_GRAPHEME -> "What is the first grapheme in '${targetPhoneme.exampleWord}'?"
            QuestionType.GRAPHEME_TO_WORD -> "Which word starts with the grapheme '${targetPhoneme.grapheme}'?"
        }

        _uiState.update {
            it.copy(
                quizState = quizState.copy(
                    targetPhoneme = targetPhoneme,
                    options = options,
                    isAnswerCorrect = null,
                    selectedOption = null,
                    questionPrompt = questionPrompt,
                    questionType = questionType
                )
            )
        }
    }
}

enum class QuestionType {
    SOUND_TO_GRAPHEME,
    GRAPHEME_TO_SOUND,
    WORD_TO_GRAPHEME,
    GRAPHEME_TO_WORD
}

data class PhoneticsUiState(
    val isLoading: Boolean = true,
    val phonemes: List<Phoneme> = emptyList(),
    val currentLevel: Int = 1,
    val isPracticeMode: Boolean = false,
    val quizState: QuizState? = null
)

data class QuizState(
    val questions: List<Phoneme>,
    val currentQuestionIndex: Int,
    val score: Int = 0,
    val isAnswerCorrect: Boolean? = null,
    val targetPhoneme: Phoneme? = null,
    val options: List<Phoneme> = emptyList(),
    val selectedOption: Phoneme? = null,
    val questionPrompt: String? = null,
    val questionType: QuestionType? = null
)

sealed class NavigationEvent {
    data class LevelComplete(val level: Int, val score: Int, val totalQuestions: Int) : NavigationEvent()
}
