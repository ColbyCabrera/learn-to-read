package com.example.readingfoundations.ui.screens.home

import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.UnitRepository
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var unitRepository: UnitRepository

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState correctly combines units and progress`() = runTest {
        val testUnits = listOf(
            Unit(
                id = 1, levels = listOf(Level(Subjects.PHONETICS, 1, false)), progress = 0f
            )
        )
        val testProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )

        `when`(unitRepository.getUnits()).thenReturn(flowOf(testUnits))
        `when`(unitRepository.getUserProgress()).thenReturn(flowOf(testProgress))

        viewModel = HomeViewModel(unitRepository)

        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testUnits, state.units)
        assertEquals(testProgress, state.userProgress)

        job.cancel()
    }

    @Test
    fun `uiState handles null user progress`() = runTest {
        val testUnits = listOf(
            Unit(
                id = 1, levels = listOf(Level(Subjects.PHONETICS, 1, false)), progress = 0f
            )
        )

        `when`(unitRepository.getUnits()).thenReturn(flowOf(testUnits))
        `when`(unitRepository.getUserProgress()).thenReturn(flowOf(null))

        viewModel = HomeViewModel(unitRepository)

        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testUnits, state.units)
        assertEquals(UserProgress(), state.userProgress)

        job.cancel()
    }
}
