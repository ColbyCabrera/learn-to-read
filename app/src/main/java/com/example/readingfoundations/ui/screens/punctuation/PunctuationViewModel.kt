package com.example.readingfoundations.ui.screens.punctuation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.PunctuationQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

import androidx.lifecycle.SavedStateHandle

class PunctuationViewModel(
    private val unitRepository: UnitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val level: Int = savedStateHandle.get<Int>("level") ?: 1
    private val _uiState = MutableStateFlow(PunctuationUiState())
    val uiState: StateFlow<PunctuationUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                // Use .first() to ensure questions are loaded only once and the quiz state is stable.
                val questions = unitRepository.getAllPunctuationQuestions().first().filter { it.level == level }.shuffled()
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
                unitRepository.updateProgress(com.example.readingfoundations.data.Subjects.PUNCTUATION, level)
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