package com.example.readingfoundations.ui.screens.punctuation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.PunctuationQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PunctuationViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PunctuationUiState())
    val uiState: StateFlow<PunctuationUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            appRepository.getAllPunctuationQuestions().collectLatest { questions ->
                _uiState.value = PunctuationUiState(
                    questions = questions.shuffled(),
                    currentQuestionIndex = 0
                )
            }
        }
    }

    fun submitAnswer(answer: String) {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = currentQuestion.correctAnswer.equals(answer, ignoreCase = true)

        _uiState.value = currentState.copy(
            isAnswerSubmitted = true,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) currentState.score + 1 else currentState.score
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                isAnswerSubmitted = false,
                isAnswerCorrect = false
            )
        } else {
            // Quiz finished
            viewModelScope.launch {
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
    val score: Int = 0
)

sealed class NavigationEvent {
    data class QuizComplete(val score: Int, val totalQuestions: Int) : NavigationEvent()
}