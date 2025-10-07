package com.example.readingfoundations.ui.screens.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.QuizRepository
import com.example.readingfoundations.data.models.QuizQuestion
import com.example.readingfoundations.data.models.allUnits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the state of the quiz screen.
 */
data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val isQuizOver: Boolean = false,
    val isLoading: Boolean = true,
    val userAnswer: String = "",
    val isAnswerCorrect: Boolean? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)
}

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val levelId: Int = checkNotNull(savedStateHandle["levelId"])

    init {
        loadQuizQuestions()
    }

    private fun loadQuizQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val level = allUnits.flatMap { it.levels }.find { it.level == levelId }
            if (level != null) {
                val questions = quizRepository.getQuizQuestions(level).first()
                _uiState.update {
                    it.copy(
                        questions = questions,
                        isLoading = false
                    )
                }
            } else {
                // Handle level not found
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(answer: String) {
        _uiState.update { it.copy(userAnswer = answer) }
    }

    fun checkAnswer() {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion
        val userAnswer = currentState.userAnswer

        if (currentQuestion != null) {
            val correctAnswer = when (currentQuestion) {
                is QuizQuestion.PhonemeQuestion -> currentQuestion.phoneme.grapheme
                is QuizQuestion.WordQuestion -> currentQuestion.word.text
                is QuizQuestion.SentenceQuestion -> currentQuestion.sentence.text
                is QuizQuestion.PunctuationQuestionItem -> currentQuestion.punctuationQuestion.correctAnswer
            }

            val isCorrect = userAnswer.trim().equals(correctAnswer, ignoreCase = true)

            _uiState.update {
                it.copy(
                    isAnswerCorrect = isCorrect,
                    score = if (isCorrect) it.score + 1 else it.score
                )
            }
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    userAnswer = "",
                    isAnswerCorrect = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isQuizOver = true,
                    isAnswerCorrect = null
                )
            }
        }
    }
}