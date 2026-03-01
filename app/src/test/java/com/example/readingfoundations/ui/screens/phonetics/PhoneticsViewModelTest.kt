package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.readingfoundations.data.PhonemeRepository
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class PhoneticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var unitRepository: UnitRepository

    @Mock
    private lateinit var phonemeRepository: PhonemeRepository

    private lateinit var viewModel: PhoneticsViewModel

    private val testPhonemes = listOf(
        Phoneme(id = 1, grapheme = "a", sound = "a", ttsText = "a", exampleWord = "apple", category = "vowel", level = 1),
        Phoneme(id = 2, grapheme = "b", sound = "b", ttsText = "b", exampleWord = "ball", category = "consonant", level = 1),
        Phoneme(id = 3, grapheme = "c", sound = "c", ttsText = "c", exampleWord = "cat", category = "consonant", level = 1),
        Phoneme(id = 4, grapheme = "d", sound = "d", ttsText = "d", exampleWord = "dog", category = "consonant", level = 1)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(phonemeRepository.getAllPhonemes()).thenReturn(flowOf(testPhonemes))

        val savedStateHandle = SavedStateHandle().apply {
            set("level", 1)
        }

        viewModel = PhoneticsViewModel(unitRepository, phonemeRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads phonemes correctly`() = runTest {
        // Wait for the initialization coroutine to complete
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testPhonemes, state.phonemes)
        assertEquals(1, state.currentLevel)
    }

    @Test
    fun `startPractice initializes quiz correctly`() = runTest {
        advanceUntilIdle()
        viewModel.startPractice()

        val state = viewModel.uiState.value
        assertTrue(state.isPracticeMode)
        assertNotNull(state.quizState)
        assertEquals(0, state.quizState?.currentQuestionIndex)
        assertEquals(0, state.quizState?.score)
        assertEquals(4, state.quizState?.questions?.size)
    }

    @Test
    fun `checkAnswer updates score correctly for correct answer`() = runTest {
        advanceUntilIdle()
        viewModel.startPractice()

        val state = viewModel.uiState.value
        val target = state.quizState!!.targetPhoneme!!

        viewModel.checkAnswer(target)

        val newState = viewModel.uiState.value
        assertEquals(true, newState.quizState!!.isAnswerCorrect)
        assertEquals(1, newState.quizState!!.score)
        assertEquals(target, newState.quizState!!.selectedOption)
    }

    @Test
    fun `checkAnswer updates state correctly for incorrect answer`() = runTest {
        advanceUntilIdle()
        viewModel.startPractice()

        val state = viewModel.uiState.value
        val target = state.quizState!!.targetPhoneme!!
        val incorrectOption = testPhonemes.first { it.id != target.id }

        viewModel.checkAnswer(incorrectOption)

        val newState = viewModel.uiState.value
        assertEquals(false, newState.quizState!!.isAnswerCorrect)
        assertEquals(0, newState.quizState!!.score)
        assertEquals(incorrectOption, newState.quizState!!.selectedOption)
    }

    @Test
    fun `nextQuestion advances to next question`() = runTest {
        advanceUntilIdle()
        viewModel.startPractice()

        val initialState = viewModel.uiState.value
        val initialIndex = initialState.quizState!!.currentQuestionIndex

        viewModel.nextQuestion()

        val newState = viewModel.uiState.value
        assertEquals(initialIndex + 1, newState.quizState!!.currentQuestionIndex)
    }

    @Test
    fun `completing quiz updates progress and navigates`() = runTest {
        advanceUntilIdle()
        viewModel.startPractice()

        // Go through all questions
        val totalQuestions = testPhonemes.size
        repeat(totalQuestions - 1) {
            viewModel.nextQuestion()
        }

        // On the last question, calling nextQuestion should finish the quiz
        viewModel.nextQuestion()
        advanceUntilIdle()

        verify(unitRepository).updateProgress(Subjects.PHONETICS, 1)

        viewModel.navigationEvent.test {
            val event = awaitItem()
            assertTrue(event is NavigationEvent.LevelComplete)
            val levelCompleteEvent = event as NavigationEvent.LevelComplete
            assertEquals(1, levelCompleteEvent.level)
            assertEquals(totalQuestions, levelCompleteEvent.totalQuestions)
        }
    }
}
