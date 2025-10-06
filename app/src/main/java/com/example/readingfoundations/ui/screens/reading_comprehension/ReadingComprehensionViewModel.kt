package com.example.readingfoundations.ui.screens.reading_comprehension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.ReadingComprehensionRepository
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadingComprehensionViewModel(
    private val repository: ReadingComprehensionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingComprehensionUiState())
    val uiState: StateFlow<ReadingComprehensionUiState> = _uiState.asStateFlow()

    private var consecutiveCorrectAnswers = 0

    init {
        loadLevel(0)
    }

    fun checkAnswer(userAnswer: String) {
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = userAnswer.trim().equals(currentQuestion.correctAnswer, ignoreCase = true)

        if (isCorrect) {
            consecutiveCorrectAnswers++
            _uiState.update { it.copy(feedback = "That's exactly right!", answerChecked = true, isCorrect = true) }
        } else {
            consecutiveCorrectAnswers = 0
            _uiState.update {
                it.copy(
                    feedback = "That's a good try! Let's look at the story again.",
                    answerChecked = true,
                    isCorrect = false
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
                    feedback = "",
                    answerChecked = false,
                    isCorrect = null
                )
            }
        } else {
            // Level complete
            if (consecutiveCorrectAnswers >= 3 && currentState.level < 4) {
                loadLevel(currentState.level + 1, "You're doing so well, let's try something a little more challenging!")
            } else {
                loadLevel(currentState.level, "Great job! Let's try another one at this level.")
            }
        }
    }

    fun previousLevel() {
        val currentLevel = _uiState.value.level
        if (currentLevel > 0) {
            loadLevel(currentLevel - 1, "No problem at all! Let's go back and practice one that's a bit easier to build our strength.")
        }
    }

    private fun loadLevel(level: Int, message: String = "") {
        viewModelScope.launch {
            val texts = repository.getTextsByLevel(level).first()
            if (texts.isNotEmpty()) {
                val text = texts.random()
                val questions = repository.getQuestionsForText(text.id).first()
                _uiState.update {
                    ReadingComprehensionUiState(
                        level = level,
                        currentText = text,
                        questions = questions,
                        feedback = message
                    )
                }
            } else {
                // Handle case where there are no texts for the level
                 _uiState.update { it.copy(feedback = "Congratulations! You have completed all levels.") }
            }
        }
        consecutiveCorrectAnswers = 0
    }
}

data class ReadingComprehensionUiState(
    val level: Int = 0,
    val currentText: ReadingComprehensionText? = null,
    val questions: List<ReadingComprehensionQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswer: String = "",
    val feedback: String = "",
    val answerChecked: Boolean = false,
    val isCorrect: Boolean? = null
)