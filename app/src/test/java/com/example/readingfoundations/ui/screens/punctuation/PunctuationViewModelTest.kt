package com.example.readingfoundations.ui.screens.punctuation

import androidx.lifecycle.SavedStateHandle
import com.example.readingfoundations.data.UnitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class PunctuationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PunctuationViewModel

    @Mock
    private lateinit var unitRepository: UnitRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        unitRepository = mock(UnitRepository::class.java)
        val savedStateHandle = SavedStateHandle().apply {
            set("level", 1)
        }
        viewModel = PunctuationViewModel(unitRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isAnswerSubmitted)
        assertFalse(uiState.isAnswerCorrect)
        assertEquals(0, uiState.score)
        assertEquals(0f, uiState.progress)
        assertEquals(0, uiState.currentQuestionIndex)
    }

    @Test
    fun `submitAnswer with correct answer updates state correctly`() {
        val uiState = viewModel.uiState.value
        val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
        viewModel.submitAnswer(currentQuestion.correctAnswer)

        val newUiState = viewModel.uiState.value
        assertTrue(newUiState.isAnswerSubmitted)
        assertTrue(newUiState.isAnswerCorrect)
        assertEquals(1, newUiState.score)
    }

    @Test
    fun `submitAnswer with incorrect answer updates state correctly`() {
        viewModel.submitAnswer("wrong answer")

        val newUiState = viewModel.uiState.value
        assertTrue(newUiState.isAnswerSubmitted)
        assertFalse(newUiState.isAnswerCorrect)
        assertEquals(0, newUiState.score)
    }

    @Test
    fun `nextQuestion updates state correctly`() {
        viewModel.nextQuestion()

        val newUiState = viewModel.uiState.value
        assertEquals(1, newUiState.currentQuestionIndex)
        assertFalse(newUiState.isAnswerSubmitted)
        assertFalse(newUiState.isAnswerCorrect)
    }

    @Test
    fun `nextQuestion on last question finishes quiz`() = runTest {
        viewModel.nextQuestion() // Go to the last question
        viewModel.nextQuestion() // Finish the quiz

        val newUiState = viewModel.uiState.value
        assertEquals(1f, newUiState.progress)

        viewModel.navigationEvent.test {
            val navigationEvent = awaitItem()
            assertTrue(navigationEvent is NavigationEvent.QuizComplete)
            assertEquals(0, (navigationEvent as NavigationEvent.QuizComplete).score)
            assertEquals(2, navigationEvent.totalQuestions)
        }
    }
}
