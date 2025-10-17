package com.example.readingfoundations.ui.screens.punctuation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.PunctuationQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

class PunctuationViewModel(private val unitRepository: UnitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PunctuationUiState())
    val uiState: StateFlow<PunctuationUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadQuestions()
    }

    /**
     * Loads punctuation questions from the repository, shuffles them, and initializes the UI state
     * with the loaded questions, a current index of 0, and the initial progress value.
     *
     * On failure, an error is logged and the UI state is left unchanged (TODO: surface an error to the user).
     */
    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                // Use .first() to ensure questions are loaded only once and the quiz state is stable.
                val questions = unitRepository.getAllPunctuationQuestions().first().shuffled()
                if (questions.isNotEmpty()) {
                    _uiState.value = PunctuationUiState(
                        questions = questions,
                        currentQuestionIndex = 0,
                        progress = 1f / questions.size
                    )
                }
            } catch (e: Exception) {
                // TODO: Update UI state to show an error to the user
                Log.e("PunctuationViewModel", "Failed to load questions", e)
            }
        }
    }

    fun submitAnswer(answer: String) {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = currentQuestion.correctAnswer.equals(answer.trim(), ignoreCase = true)

        _uiState.value = currentState.copy(
            isAnswerSubmitted = true,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) currentState.score + 1 else currentState.score
        )
    }

    /**
     * Advances the quiz to the next question or completes the quiz if on the last question.
     *
     * If there are more questions, increments the current question index, resets answer-related flags,
     * and updates progress. If the current question is the last one, records completion in the repository
     * and emits a QuizComplete navigation event with the final score and total question count.
     */
    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                isAnswerSubmitted = false,
                isAnswerCorrect = false,
                progress = (nextIndex).toFloat() / currentState.questions.size
            )
        } else {
            // Quiz finished
            viewModelScope.launch {
                unitRepository.updateProgress("Punctuation", 1)
                _navigationEvent.send(NavigationEvent.QuizComplete(currentState.score, currentState.questions.size))
            }
        }
    }
}

data class PunctuationUiState(
    val questions: List<PunctuationQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val score: Int = 0,
    val progress: Float = 0f
)

sealed class NavigationEvent {
    data class QuizComplete(val score: Int, val totalQuestions: Int) : NavigationEvent()
}