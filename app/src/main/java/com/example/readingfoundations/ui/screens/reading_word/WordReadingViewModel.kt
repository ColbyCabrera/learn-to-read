package com.example.readingfoundations.ui.screens.reading_word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.data.AppRepository
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WordReadingViewModel(
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val level: Int = checkNotNull(savedStateHandle["levelNumber"])

    private val _uiState = MutableStateFlow(WordReadingUiState())
    val uiState: StateFlow<WordReadingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            appRepository.getWordsByDifficulty(level).collect { words ->
                _uiState.value = WordReadingUiState(
                    words = words,
                    currentLevel = level,
                    isPracticeMode = false,
                    quizState = null
                )
            }
        }
    }

    fun startPractice() {
        _uiState.value = _uiState.value.copy(
            isPracticeMode = true,
            quizState = QuizState(
                questions = _uiState.value.words.shuffled(),
                currentQuestionIndex = 0
            )
        )
    }

    fun submitAnswer(answer: String) {
        val quizState = _uiState.value.quizState ?: return
        val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
        val isCorrect = currentQuestion.text.equals(answer, ignoreCase = true)

        val updatedQuestions = quizState.questions.toMutableList()
        updatedQuestions[quizState.currentQuestionIndex] = currentQuestion

        val newCorrectAnswers = if (isCorrect) quizState.correctAnswers + 1 else quizState.correctAnswers

        val newQuizState = quizState.copy(
            questions = updatedQuestions,
            isAnswerCorrect = isCorrect,
            correctAnswers = newCorrectAnswers
        )
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
                val updatedLevels = userProgress.wordLevels.map {
                    if (it.levelNumber == level) {
                        it.copy(questionsCorrect = quizState.correctAnswers, isCompleted = true)
                    } else {
                        it
                    }
                }
                appRepository.updateUserProgress(
                    userProgress.copy(wordLevels = updatedLevels)
                )
                _navigationEvent.send(NavigationEvent.LevelComplete(level))
            }
        }
    }
}

data class WordReadingUiState(
    val words: List<Word> = emptyList(),
    val currentLevel: Int = 1,
    val isPracticeMode: Boolean = false,
    val quizState: QuizState? = null
)

data class QuizState(
    val questions: List<Word>,
    val currentQuestionIndex: Int,
    val isAnswerCorrect: Boolean? = null,
    val correctAnswers: Int = 0
)

sealed class NavigationEvent {
    data class LevelComplete(val level: Int) : NavigationEvent()
}