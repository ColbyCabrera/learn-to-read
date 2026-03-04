package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.local.PunctuationQuestionDao
import com.example.readingfoundations.data.local.ReadingComprehensionDao
import com.example.readingfoundations.data.local.SentenceDao
import com.example.readingfoundations.data.local.UserProgressDao
import com.example.readingfoundations.data.local.WordDao
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.system.measureTimeMillis

class UnitRepositoryBenchmarkTest {

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
    fun benchmarkGetUnits() = runTest {
        // Create a large number of max levels
        val maxLevel = 1000
        val completedLevelsList = (1..maxLevel).toList()
        val userProgress = UserProgress(
            completedLevels = mapOf(
                Subjects.PHONETICS to completedLevelsList,
                Subjects.WORD_BUILDING to completedLevelsList,
                Subjects.SENTENCE_READING to completedLevelsList,
                Subjects.PUNCTUATION to completedLevelsList,
                Subjects.READING_COMPREHENSION to completedLevelsList
            )
        )

        `when`(userProgressDao.getUserProgress()).thenReturn(flowOf(userProgress))
        `when`(phonemeDao.getHighestLevel()).thenReturn(flowOf(maxLevel))
        `when`(wordDao.getHighestDifficulty()).thenReturn(flowOf(maxLevel))
        `when`(sentenceDao.getHighestDifficulty()).thenReturn(flowOf(maxLevel))
        `when`(punctuationQuestionDao.getHighestLevel()).thenReturn(flowOf(maxLevel))
        `when`(readingComprehensionDao.getHighestLevel()).thenReturn(flowOf(maxLevel))

        // Warmup
        unitRepository.getUnits().first()

        val time = measureTimeMillis {
            for (i in 1..100) {
                unitRepository.getUnits().first()
            }
        }
        println("Time taken: $time ms")
    }
}
