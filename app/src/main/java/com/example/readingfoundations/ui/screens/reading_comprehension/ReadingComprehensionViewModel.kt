package com.example.readingfoundations.ui.screens.reading_comprehension

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.ReadingComprehensionRepository
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
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

sealed class NavigationEvent {
    data class LevelComplete(val level: Int, val score: Int, val totalQuestions: Int) :
        NavigationEvent()
}

class ReadingComprehensionViewModel(
    private val repository: ReadingComprehensionRepository,
    private val unitRepository: UnitRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingComprehensionUiState())
    val uiState: StateFlow<ReadingComprehensionUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<ReadingComprehensionEvent>()
    val events = _eventChannel.receiveAsFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var consecutiveCorrectAnswers = 0
    private var consecutiveIncorrectAnswers = 0
    private var nextLevel: Int? = null
    private val level: Int = savedStateHandle["level"] ?: 0

    init {
        loadLevel(level)
    }

    fun checkAnswer(userAnswer: String) {
        val currentQuestionWrapper = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = userAnswer.trim().equals(currentQuestionWrapper.question.correctAnswer, ignoreCase = true)
        val currentLevel = _uiState.value.level

        if (isCorrect) {
            consecutiveCorrectAnswers++
            consecutiveIncorrectAnswers = 0
            _uiState.update {
                val updatedQuestions = it.questions.toMutableList()
                updatedQuestions[it.currentQuestionIndex] = updatedQuestions[it.currentQuestionIndex].copy(isCorrect = true)
                it.copy(
                    feedback = "That's exactly right!",
                    answerChecked = true,
                    isCorrect = true,
                    questions = updatedQuestions
                )
            }

            if (consecutiveCorrectAnswers >= CORRECT_ANSWERS_TO_LEVEL_UP && currentLevel < MAX_LEVEL) {
                nextLevel = currentLevel + 1
            }
        } else {
            consecutiveCorrectAnswers = 0
            consecutiveIncorrectAnswers++
            val feedback = "That's a good try! Let's look at this part of the story again: \"${_uiState.value.currentText?.text}\""
            _uiState.update {
                val updatedQuestions = it.questions.toMutableList()
                updatedQuestions[it.currentQuestionIndex] = updatedQuestions[it.currentQuestionIndex].copy(isCorrect = false)
                it.copy(
                    feedback = feedback,
                    answerChecked = true,
                    isCorrect = false,
                    questions = updatedQuestions
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
                    isCorrect = null,
                    currentProgress = it.currentProgress + 1
                )
            }
        } else {
            // Level complete, check if we need to change levels
            viewModelScope.launch {
                unitRepository.updateProgress(Subjects.READING_COMPREHENSION, currentState.level)
            }
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
                viewModelScope.launch {
                    _navigationEvent.send(
                        NavigationEvent.LevelComplete(
                            level = currentState.level,
                            score = uiState.value.questions.count { it.isCorrect ?: false },
                            totalQuestions = uiState.value.questions.size
                        )
                    )
                }
            }
        }
    }

    fun previousLevel() {
        val currentLevel = _uiState.value.level
        if (currentLevel > 0) {
            loadLevel(currentLevel - 1, "No problem at all! Let's go back and practice one that's a bit easier to build our strength.")
        }
    }

    fun loadNextLevel() {
        val currentLevel = _uiState.value.level
        if (currentLevel < MAX_LEVEL) {
            loadLevel(currentLevel + 1)
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
                        questions = questions.map { ReadingComprehensionQuestionWrapper(question = it) },
                        feedback = message,
                        totalQuestions = questions.size,
                        currentProgress = 1
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

data class ReadingComprehensionQuestionWrapper(
    val question: ReadingComprehensionQuestion,
    val isCorrect: Boolean? = null
)

data class ReadingComprehensionUiState(
    val level: Int = 0,
    val currentText: ReadingComprehensionText? = null,
    val questions: List<ReadingComprehensionQuestionWrapper> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val feedback: String = "",
    val answerChecked: Boolean = false,
    val isCorrect: Boolean? = null,
    val currentProgress: Int = 0,
    val totalQuestions: Int = 0
)