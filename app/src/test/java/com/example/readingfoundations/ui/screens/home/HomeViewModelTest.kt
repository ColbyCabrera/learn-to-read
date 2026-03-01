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
                id = 1,
                levels = listOf(Level(Subjects.PHONETICS, 1, false)),
                progress = 0f
            )
        )
        val testProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )

        `when`(unitRepository.getUnits()).thenReturn(flowOf(testUnits))
        `when`(unitRepository.getUserProgress()).thenReturn(flowOf(testProgress))

        viewModel = HomeViewModel(unitRepository)

        // Ensure the StateFlow is collected to trigger the upstream flows
        // In a real app, the UI collects it. In a test, accessing .value usually gives the initial value
        // until collection starts or SharingStarted.Eagerly is used.
        // However, with WhileSubscribed, it waits for a subscriber.

        // We can use a background collection job
        // Or we can just access it? No, accessing .value doesn't start collection for WhileSubscribed.

        // Let's use turbine or a simple background collection

        // Actually, stateIn with WhileSubscribed(5000) will keep the value cached for 5s after subscription ends.
        // But it needs a subscription to start.

        // Wait, I see other tests using stateIn often require a collector.

        // Let's try collecting it in a background job
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
                id = 1,
                levels = listOf(Level(Subjects.PHONETICS, 1, false)),
                progress = 0f
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
