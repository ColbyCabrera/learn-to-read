package com.example.readingfoundations.ui.screens.punctuation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.PunctuationQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PunctuationViewModel(
    private val unitRepository: UnitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val level: Int = savedStateHandle.get<Int>("level") ?: 1

    private val _uiState = MutableStateFlow(PunctuationUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        // Replace with actual data loading logic
        val dummyQuestions = listOf(
            PunctuationQuestion(
                id = 1,
                text = "Which sentence needs a period?",
                correctAnswer = "The dog is running",
                options = listOf("The dog is running", "Wow", "Is it raining"),
                level = 1
            ),
            PunctuationQuestion(
                id = 2,
                text = "Which punctuation mark shows excitement?",
                correctAnswer = "!",
                options = listOf(".", "!", "?"),
                level = 1
            )
        )
        _uiState.value = PunctuationUiState(questions = dummyQuestions)
    }

    fun submitAnswer(answer: String) {
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = answer.equals(currentQuestion.correctAnswer, ignoreCase = true)
        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                isAnswerCorrect = isCorrect,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    isAnswerSubmitted = false,
                    isAnswerCorrect = false,
                    progress = (it.currentQuestionIndex + 1).toFloat() / it.questions.size
                )
            }
        } else {
            // Quiz finished
            _uiState.update { it.copy(progress = 1f) }
            viewModelScope.launch {
                _navigationEvent.send(NavigationEvent.QuizComplete(_uiState.value.score, _uiState.value.questions.size))
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