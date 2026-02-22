package com.example.readingfoundations.ui.screens.phonetics

import androidx.lifecycle.SavedStateHandle
import com.example.readingfoundations.data.PhonemeRepository
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneticsViewModelTest {

    private val unitRepository: UnitRepository = mock()
    private val phonemeRepository: PhonemeRepository = mock()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("level" to 1))

    private lateinit var viewModel: PhoneticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startPractice_initializesQuizState_withQuestions() = runTest {
        val phonemes = listOf(
            Phoneme(1, "a", "a", "a as in apple", "apple", "vowel", 1),
            Phoneme(2, "b", "b", "b as in ball", "ball", "consonant", 1),
            Phoneme(3, "c", "c", "c as in cat", "cat", "consonant", 1),
            Phoneme(4, "d", "d", "d as in dog", "dog", "consonant", 1)
        )
        whenever(phonemeRepository.getAllPhonemes()).thenReturn(flowOf(phonemes))

        viewModel = PhoneticsViewModel(unitRepository, phonemeRepository, savedStateHandle)

        // Allow init block to collect flow
        advanceUntilIdle()

        viewModel.startPractice()

        // Allow generateNewQuestion logic to complete if any coroutines are involved (though it seems synchronous except for state updates)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue("Practice mode should be true", uiState.isPracticeMode)
        assertNotNull("Quiz state should not be null", uiState.quizState)
        assertTrue("Questions list should not be empty", uiState.quizState!!.questions.isNotEmpty())
        assertNotNull("Target phoneme should not be null", uiState.quizState!!.targetPhoneme)
        assertTrue("Options should not be empty", uiState.quizState!!.options.isNotEmpty())
    }
}
