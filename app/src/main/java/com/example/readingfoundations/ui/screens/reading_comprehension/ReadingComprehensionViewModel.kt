package com.example.readingfoundations.ui.screens.reading_comprehension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.ReadingComprehensionRepository
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ReadingComprehensionEvent {
    data class ShowSnackbar(val message: String) : ReadingComprehensionEvent()
}

class ReadingComprehensionViewModel(
    private val repository: ReadingComprehensionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingComprehensionUiState())
    val uiState: StateFlow<ReadingComprehensionUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<ReadingComprehensionEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var consecutiveCorrectAnswers = 0
    private var consecutiveIncorrectAnswers = 0
    private var nextLevel: Int? = null

    init {
        loadLevel(0)
    }

    fun checkAnswer(userAnswer: String) {
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = userAnswer.trim().equals(currentQuestion.correctAnswer, ignoreCase = true)
        val currentLevel = _uiState.value.level

        if (isCorrect) {
            consecutiveCorrectAnswers++
            consecutiveIncorrectAnswers = 0
            _uiState.update { it.copy(feedback = "That's exactly right!", answerChecked = true, isCorrect = true) }

            if (consecutiveCorrectAnswers >= CORRECT_ANSWERS_TO_LEVEL_UP && currentLevel < MAX_LEVEL) {
                nextLevel = currentLevel + 1
            }
        } else {
            consecutiveCorrectAnswers = 0
            consecutiveIncorrectAnswers++
            val feedback = "That's a good try! Let's look at this part of the story again: \"${_uiState.value.currentText?.text}\""
            _uiState.update {
                it.copy(
                    feedback = feedback,
                    answerChecked = true,
                    isCorrect = false
                )
            }

            if (consecutiveIncorrectAnswers >= INCORRECT_ANSWERS_TO_LEVEL_DOWN && currentLevel > 0) {
                nextLevel = currentLevel - 1
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
            // Level complete, check if we need to change levels
            val levelToLoad = nextLevel
            if (levelToLoad != null) {
                val message = if (levelToLoad > currentState.level) {
                    "You're doing so well, let's try something a little more challenging!"
                } else {
                    "No problem at all! Let's go back and practice one that's a bit easier to build our strength."
                }
                viewModelScope.launch {
                    _eventChannel.send(ReadingComprehensionEvent.ShowSnackbar(message))
                }
                loadLevel(levelToLoad)
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
        consecutiveCorrectAnswers = 0
        consecutiveIncorrectAnswers = 0
        nextLevel = null
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
    }

    companion object {
        private const val CORRECT_ANSWERS_TO_LEVEL_UP = 3
        private const val INCORRECT_ANSWERS_TO_LEVEL_DOWN = 2
        private const val MAX_LEVEL = 4
    }
}

data class ReadingComprehensionUiState(
    val level: Int = 0,
    val currentText: ReadingComprehensionText? = null,
    val questions: List<ReadingComprehensionQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val feedback: String = "",
    val answerChecked: Boolean = false,
    val isCorrect: Boolean? = null
)