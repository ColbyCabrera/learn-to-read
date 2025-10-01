package com.example.readingfoundations.ui.screens.reading_sentence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SentenceReadingViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SentenceReadingUiState())
    val uiState: StateFlow<SentenceReadingUiState> = _uiState.asStateFlow()

    init {
        loadSentences()
    }

    private fun loadSentences() {
        viewModelScope.launch {
            val userProgress = appRepository.getUserProgress().first() ?: UserProgress()
            val currentLevel = userProgress.lastSentenceLevelCompleted + 1
            appRepository.getSentencesByDifficulty(currentLevel).collect { sentences ->
                _uiState.value = SentenceReadingUiState(
                    sentences = sentences,
                    currentLevel = currentLevel,
                    isPracticeMode = false,
                    quizState = null
                )
            }
        }
    }

    fun startPractice() {
        _uiState.value = _uiState.value.copy(
            isPracticeMode = true,
            quizState = SentenceQuizState(
                questions = _uiState.value.sentences.shuffled(),
                currentQuestionIndex = 0
            )
        )
    }

    fun submitAnswer(answer: String) {
        val quizState = _uiState.value.quizState ?: return
        val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
        val isCorrect = currentQuestion.text.equals(answer, ignoreCase = true)

        val newQuizState = quizState.copy(isAnswerCorrect = isCorrect)
        _uiState.value = _uiState.value.copy(quizState = newQuizState)
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
                appRepository.updateUserProgress(
                    userProgress.copy(lastSentenceLevelCompleted = _uiState.value.currentLevel)
                )
                loadSentences() // Load next level
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
    val isAnswerCorrect: Boolean? = null
)