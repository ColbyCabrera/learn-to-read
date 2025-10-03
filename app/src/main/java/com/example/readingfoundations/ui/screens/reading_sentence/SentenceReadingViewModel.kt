package com.example.readingfoundations.ui.screens.reading_sentence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SentenceReadingViewModel(
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentenceReadingUiState())
    val uiState: StateFlow<SentenceReadingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val level: Int = savedStateHandle.get<Int>("level") ?: 1

    init {
        loadSentences(level)
    }

    private fun loadSentences(level: Int) {
        viewModelScope.launch {
            appRepository.getSentencesByDifficulty(level).collect { sentences ->
                _uiState.value = SentenceReadingUiState(
                    sentences = sentences,
                    currentLevel = level
                )
            }
        }
    }

    fun startPractice() {
        _uiState.value = _uiState.value.copy(
            isPracticeMode = true,
            quizState = SentenceQuizState(
                questions = _uiState.value.sentences.shuffled(),
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
                val userProgress = appRepository.getUserProgress().first() ?: UserProgress()
                val score = quizState.score
                val totalQuestions = quizState.questions.size
                val progressPercentage = (score.toFloat() / totalQuestions * 100).toInt()

                val updatedProgress = userProgress.sentenceLevelsProgress.toMutableMap()
                updatedProgress[level] = progressPercentage

                appRepository.updateUserProgress(
                    userProgress.copy(sentenceLevelsProgress = updatedProgress)
                )
                _navigationEvent.send(
                    NavigationEvent.LevelComplete(
                        level = level,
                        score = score,
                        totalQuestions = totalQuestions
                    )
                )
            }
        }
    }
}

data class SentenceReadingUiState(
    val sentences: List<Sentence> = emptyList(),
    val currentLevel: Int = 1,
    val isPracticeMode: Boolean = false,
    val quizState: SentenceQuizState? = null
)

data class SentenceQuizState(
    val questions: List<Sentence>,
    val currentQuestionIndex: Int,
    val score: Int = 0,
    val isAnswerCorrect: Boolean? = null
)

sealed class NavigationEvent {
    data class LevelComplete(val level: Int, val score: Int, val totalQuestions: Int) :
        NavigationEvent()
}