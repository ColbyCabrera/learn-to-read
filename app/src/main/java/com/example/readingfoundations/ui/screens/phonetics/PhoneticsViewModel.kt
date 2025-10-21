package com.example.readingfoundations.ui.screens.phonetics

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingfoundations.R
import com.example.readingfoundations.data.PhonemeRepository
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneticsViewModel(
    private val unitRepository: UnitRepository,
    private val phonemeRepository: PhonemeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val level: Int = savedStateHandle.get<Int>("level") ?: 1
    private val _uiState = MutableStateFlow(PhoneticsUiState())
    val uiState: StateFlow<PhoneticsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>(capacity = Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var allPhonemes: List<Phoneme> = emptyList()
    private var levelPhonemes: List<Phoneme> = emptyList()

    init {
        viewModelScope.launch {
            try {
                phonemeRepository.getAllPhonemes().collect { list ->
                    allPhonemes = list
                    levelPhonemes = list.filter { it.level == level }
                    _uiState.update { it.copy(isLoading = false, phonemes = levelPhonemes, currentLevel = level) }
                }
            } catch (e: Exception) {
                Log.e("PhoneticsViewModel", "Failed to load phonemes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        phonemes = emptyList()
                    )
                }
            }
        }
    }

    fun startPractice() {
        val quizState = QuizState(
            questions = levelPhonemes.shuffled(),
            currentQuestionIndex = 0,
            score = 0
        )
        _uiState.update { it.copy(isPracticeMode = true, quizState = quizState) }
        generateNewQuestion()
    }

    fun checkAnswer(selectedOption: Phoneme) {
        val quizState = _uiState.value.quizState ?: return
        val isCorrect = selectedOption.id == quizState.targetPhoneme?.id
        val newScore = if (isCorrect) quizState.score + 1 else quizState.score

        _uiState.update {
            it.copy(
                quizState = quizState.copy(
                    isAnswerCorrect = isCorrect,
                    selectedOption = selectedOption,
                    score = newScore
                )
            )
        }
    }

    fun nextQuestion() {
        val quizState = _uiState.value.quizState ?: return
        if (quizState.currentQuestionIndex < quizState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    quizState = quizState.copy(
                        currentQuestionIndex = quizState.currentQuestionIndex + 1,
                        isAnswerCorrect = null,
                        selectedOption = null
                    )
                )
            }
            generateNewQuestion()
        } else {
            // Quiz finished
            viewModelScope.launch {
                val score = quizState.score
                try {
                    unitRepository.updateProgress(com.example.readingfoundations.data.Subjects.PHONETICS, level)
                } catch (e: Exception) {
                    Log.e("PhoneticsViewModel", "Failed to update progress", e)
                }
                _navigationEvent.trySend(
                    NavigationEvent.LevelComplete(
                        level = level,
                        score = score,
                        totalQuestions = quizState.questions.size
                    )
                )
            }
        }
    }

    private fun generateNewQuestion() {
        val quizState = _uiState.value.quizState ?: return
        if (levelPhonemes.isEmpty()) {
            _uiState.update {
                it.copy(
                    quizState = quizState.copy(
                        questionPrompt = UiText.StringResource(R.string.not_enough_phonemes),
                        options = emptyList()
                    )
                )
            }
            return
        }

        val targetPhoneme: Phoneme = quizState.questions[quizState.currentQuestionIndex]

        // Determine valid question types for this phoneme
        val isOnsetPhoneme = targetPhoneme.exampleWord.startsWith(targetPhoneme.grapheme, ignoreCase = true)
        val validQuestionTypes = if (isOnsetPhoneme) {
            QuestionType.entries.toTypedArray()
        } else {
            arrayOf(QuestionType.SOUND_TO_GRAPHEME, QuestionType.GRAPHEME_TO_SOUND)
        }
        val questionType = validQuestionTypes.random()


        val getDisplayLabel: (Phoneme, QuestionType) -> String = { phoneme, type ->
            when (type) {
                QuestionType.GRAPHEME_TO_WORD -> phoneme.exampleWord
                QuestionType.GRAPHEME_TO_SOUND -> phoneme.sound
                else -> phoneme.grapheme
            }
        }

        val sameLevel = levelPhonemes.filter { it.id != targetPhoneme.id }.shuffled()
        val globalOthers = (allPhonemes - levelPhonemes.toSet()).filter { it.id != targetPhoneme.id }.shuffled()
        val potentialOptions = sameLevel + globalOthers
        val otherOptions = mutableListOf<Phoneme>()
        val usedLabels = mutableSetOf(getDisplayLabel(targetPhoneme, questionType))

        for (phoneme in potentialOptions) {
            if (otherOptions.size >= 3) break
            val label = getDisplayLabel(phoneme, questionType)
            if (label !in usedLabels) {
                otherOptions.add(phoneme)
                usedLabels.add(label)
            }
        }

        if (otherOptions.size < 3) {
            val remainingOptions = potentialOptions.filter { it !in otherOptions }
            for (phoneme in remainingOptions) {
                if (otherOptions.size >= 3) break
                otherOptions.add(phoneme)
            }
        }

        val options = (otherOptions + targetPhoneme).shuffled()


        val questionPrompt = when (questionType) {
            QuestionType.SOUND_TO_GRAPHEME -> UiText.StringResource(R.string.question_sound_to_grapheme, targetPhoneme.sound)
            QuestionType.GRAPHEME_TO_SOUND -> UiText.StringResource(R.string.question_grapheme_to_sound, targetPhoneme.grapheme)
            QuestionType.WORD_TO_GRAPHEME -> UiText.StringResource(R.string.question_word_to_grapheme, targetPhoneme.exampleWord)
            QuestionType.GRAPHEME_TO_WORD -> UiText.StringResource(R.string.question_grapheme_to_word, targetPhoneme.grapheme)
        }

        _uiState.update {
            it.copy(
                quizState = quizState.copy(
                    targetPhoneme = targetPhoneme,
                    options = options,
                    isAnswerCorrect = null,
                    selectedOption = null,
                    questionPrompt = questionPrompt,
                    questionType = questionType
                )
            )
        }
    }
}

sealed class UiText {
    class StringResource(@param:StringRes val resId: Int, vararg val args: Any) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringResource

            if (resId != other.resId) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}

enum class QuestionType {
    SOUND_TO_GRAPHEME,
    GRAPHEME_TO_SOUND,
    WORD_TO_GRAPHEME,
    GRAPHEME_TO_WORD
}

data class PhoneticsUiState(
    val isLoading: Boolean = true,
    val phonemes: List<Phoneme> = emptyList(),
    val currentLevel: Int = 1,
    val isPracticeMode: Boolean = false,
    val quizState: QuizState? = null
)

data class QuizState(
    val questions: List<Phoneme>,
    val currentQuestionIndex: Int,
    val score: Int = 0,
    val isAnswerCorrect: Boolean? = null,
    val targetPhoneme: Phoneme? = null,
    val options: List<Phoneme> = emptyList(),
    val selectedOption: Phoneme? = null,
    val questionPrompt: UiText? = null,
    val questionType: QuestionType? = null
)

sealed class NavigationEvent {
    data class LevelComplete(val level: Int, val score: Int, val totalQuestions: Int) : NavigationEvent()
}
