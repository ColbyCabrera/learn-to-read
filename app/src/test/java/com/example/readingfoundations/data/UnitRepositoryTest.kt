package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.local.PunctuationQuestionDao
import com.example.readingfoundations.data.local.ReadingComprehensionDao
import com.example.readingfoundations.data.local.SentenceDao
import com.example.readingfoundations.data.local.UserProgressDao
import com.example.readingfoundations.data.local.WordDao
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class UnitRepositoryTest {

    @Mock
    private lateinit var userProgressDao: UserProgressDao

    @Mock
    private lateinit var phonemeDao: PhonemeDao

    @Mock
    private lateinit var wordDao: WordDao

    @Mock
    private lateinit var sentenceDao: SentenceDao

    @Mock
    private lateinit var punctuationQuestionDao: PunctuationQuestionDao

    @Mock
    private lateinit var readingComprehensionDao: ReadingComprehensionDao

    private lateinit var unitRepository: UnitRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        unitRepository = UnitRepositoryImpl(
            userProgressDao,
            phonemeDao,
            wordDao,
            sentenceDao,
            punctuationQuestionDao,
            readingComprehensionDao
        )
    }

    @Test
    fun `getUnits returns correct units based on max levels`() = runTest {
        // Mock data
        val userProgress = UserProgress()
        `when`(userProgressDao.getUserProgress()).thenReturn(flowOf(userProgress))
        `when`(phonemeDao.getHighestLevel()).thenReturn(flowOf(2)) // 1 unit
        `when`(wordDao.getHighestDifficulty()).thenReturn(flowOf(0))
        `when`(sentenceDao.getHighestDifficulty()).thenReturn(flowOf(0))
        `when`(punctuationQuestionDao.getHighestLevel()).thenReturn(flowOf(0))
        `when`(readingComprehensionDao.getHighestLevel()).thenReturn(flowOf(0))

        val units = unitRepository.getUnits().first()

        assertEquals(1, units.size)
        val unit = units[0]
        assertEquals(1, unit.id)
        assertEquals(2, unit.levels.size) // 2 phonetics levels
        assertEquals(Subjects.PHONETICS, unit.levels[0].subject)
        assertEquals(1, unit.levels[0].levelNumber)
        assertEquals(Subjects.PHONETICS, unit.levels[1].subject)
        assertEquals(2, unit.levels[1].levelNumber)
    }

    @Test
    fun `getUnits calculates progress correctly`() = runTest {
        // Mock data
        val userProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )
        `when`(userProgressDao.getUserProgress()).thenReturn(flowOf(userProgress))
        `when`(phonemeDao.getHighestLevel()).thenReturn(flowOf(2))
        `when`(wordDao.getHighestDifficulty()).thenReturn(flowOf(0))
        `when`(sentenceDao.getHighestDifficulty()).thenReturn(flowOf(0))
        `when`(punctuationQuestionDao.getHighestLevel()).thenReturn(flowOf(0))
        `when`(readingComprehensionDao.getHighestLevel()).thenReturn(flowOf(0))

        val units = unitRepository.getUnits().first()

        assertEquals(1, units.size)
        val unit = units[0]
        assertEquals(0.5f, unit.progress, 0.01f) // 1 completed out of 2 levels
        assertEquals(true, unit.levels[0].isCompleted)
        assertEquals(false, unit.levels[1].isCompleted)
    }

    @Test
    fun `updateProgress updates user progress correctly`() = runTest {
        val initialProgress = UserProgress()
        `when`(userProgressDao.getUserProgress()).thenReturn(flowOf(initialProgress))

        unitRepository.updateProgress(Subjects.PHONETICS, 1)

        val expectedProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )
        verify(userProgressDao).updateUserProgress(expectedProgress)
    }

    @Test
    fun `updateProgress does not duplicate existing levels`() = runTest {
        val initialProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )
        `when`(userProgressDao.getUserProgress()).thenReturn(flowOf(initialProgress))

        unitRepository.updateProgress(Subjects.PHONETICS, 1)

        val expectedProgress = UserProgress(
            completedLevels = mapOf(Subjects.PHONETICS to listOf(1))
        )
        verify(userProgressDao).updateUserProgress(expectedProgress)
    }
}
