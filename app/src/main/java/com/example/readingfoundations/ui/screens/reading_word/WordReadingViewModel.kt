package com.example.readingfoundations.ui.screens.reading_word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WordReadingViewModel(
    private val unitRepository: UnitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordReadingUiState())
    val uiState: StateFlow<WordReadingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val level: Int = savedStateHandle.get<Int>("level") ?: 1

    init {
        loadWords(level)
    }

    /**
     * Loads words for the specified difficulty level and updates the UI state with the results.
     *
     * Updates the ViewModel's UI state so `words` contains the retrieved list and `currentLevel` is set to `level`.
     *
     * @param level The difficulty level whose words should be loaded.
     */
    private fun loadWords(level: Int) {
        viewModelScope.launch {
            unitRepository.getWordsByDifficulty(level).collect { words ->
                _uiState.value = WordReadingUiState(
                    words = words,
                    currentLevel = level
                )
            }
        }
    }

    fun startPractice() {
        _uiState.value = _uiState.value.copy(
            isPracticeMode = true,
            quizState = QuizState(
                questions = _uiState.value.words.shuffled(),
                currentQuestionIndex = 0,
                score = 0
            )
        )
    }

    fun submitAnswer(answer: String) {
        val quizState = _uiState.value.quizState ?: return
        val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
        val isCorrect = currentQuestion.text.equals(answer.trim(), ignoreCase = true)

        val newScore = if (isCorrect) quizState.score + 1 else quizState.score

        _uiState.value = _uiState.value.copy(
            quizState = quizState.copy(
                isAnswerCorrect = isCorrect,
                score = newScore
            )
        )
    }

    /**
     * Advances the quiz to the next question or finishes the quiz if the current question is the last.
     *
     * When a next question exists, increments the quiz's currentQuestionIndex and resets the answer correctness flag.
     * When the quiz is finished, updates progress for the current level and emits a LevelComplete navigation event with the level, final score, and total question count.
     */
    fun nextQuestion() {
        val quizState = _uiState.value.quizState ?: return
        if (quizState.currentQuestionIndex < quizState.questions.size - 1) {
            _uiState.value = _uiState.value.copy(
                quizState = quizState.copy(
                    currentQuestionIndex = quizState.currentQuestionIndex + 1,
                    isAnswerCorrect = null
                )
            )
        } else {
            // Quiz finished
            viewModelScope.launch {
                val score = quizState.score
                unitRepository.updateProgress("Word Building", level)
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
}

data class WordReadingUiState(
    val words: List<Word> = emptyList(),
    val currentLevel: Int = 1,
    val isPracticeMode: Boolean = false,
    val quizState: QuizState? = null,
)

data class QuizState(
    val questions: List<Word>,
    val currentQuestionIndex: Int,
    val score: Int = 0,
    val isAnswerCorrect: Boolean? = null
)

sealed class NavigationEvent {
    data class LevelComplete(val level: Int, val score: Int, val totalQuestions: Int) : NavigationEvent()
}