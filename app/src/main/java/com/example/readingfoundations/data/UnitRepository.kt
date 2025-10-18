package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.local.PunctuationQuestionDao
import com.example.readingfoundations.data.local.ReadingComprehensionDao
import com.example.readingfoundations.data.local.SentenceDao
import com.example.readingfoundations.data.local.UserProgressDao
import com.example.readingfoundations.data.local.WordDao
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface UnitRepository {
    fun getUnits(): Flow<List<Unit>>
    suspend fun updateProgress(subject: String, level: Int)
    fun getUserProgress(): Flow<UserProgress?>
    fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>>
    fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>>
    fun getAllPunctuationQuestions(): Flow<List<PunctuationQuestion>>
    fun getReadingComprehensionQuestions(level: Int): Flow<List<ReadingComprehensionQuestion>>
}

@Singleton
class UnitRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val phonemeDao: PhonemeDao,
    private val wordDao: WordDao,
    private val sentenceDao: SentenceDao,
    private val punctuationQuestionDao: PunctuationQuestionDao,
    private val readingComprehensionDao: ReadingComprehensionDao
) : UnitRepository {

    override fun getUnits(): Flow<List<Unit>> {
        val progressFlow = userProgressDao.getUserProgress()
        val phonemeLevelFlow = phonemeDao.getHighestLevel()
        val wordLevelFlow = wordDao.getHighestDifficulty()
        val sentenceLevelFlow = sentenceDao.getHighestDifficulty()
        val punctuationLevelFlow = punctuationQuestionDao.getHighestLevel()
        val readingComprehensionLevelFlow = readingComprehensionDao.getHighestLevel()

        return combine(
            listOf(
                progressFlow,
                phonemeLevelFlow,
                wordLevelFlow,
                sentenceLevelFlow,
                punctuationLevelFlow,
                readingComprehensionLevelFlow
            )
        ) { array ->
            val userProgress = array[0] as UserProgress?
            val maxPhonemeLevel = array[1] as Int
            val maxWordLevel = array[2] as Int
            val maxSentenceLevel = array[3] as Int
            val maxPunctuationLevel = array[4] as Int
            val maxReadingComprehensionLevel = array[5] as Int

            val minLevels = minOf(
                maxPhonemeLevel,
                maxWordLevel,
                maxSentenceLevel,
                maxPunctuationLevel,
                maxReadingComprehensionLevel
            )
            val totalUnits = (minLevels + 1) / 2

            (1..totalUnits).map { unitId ->
                val levels = mutableListOf<Level>()
                val subjects = mapOf(
                    Subjects.PHONETICS to maxPhonemeLevel,
                    Subjects.WORD_BUILDING to maxWordLevel,
                    Subjects.SENTENCE_READING to maxSentenceLevel,
                    Subjects.PUNCTUATION to maxPunctuationLevel,
                    Subjects.READING_COMPREHENSION to maxReadingComprehensionLevel
                )
                for ((subject, maxLevel) in subjects) {
                    for (i in 1..2) {
                        val levelNumber = (unitId - 1) * 2 + i
                        if (levelNumber > maxLevel) continue
                        val isCompleted =
                            userProgress?.completedLevels?.get(subject)?.contains(levelNumber)
                                ?: false
                        levels.add(Level(subject, levelNumber, isCompleted))
                    }
                }
                val completedCount = levels.count { it.isCompleted }
                val progress =
                    if (levels.isNotEmpty()) completedCount.toFloat() / levels.size else 0f
                Unit(unitId, levels, progress)
            }
        }
    }

    override suspend fun updateProgress(subject: String, level: Int) {
        val userProgress = userProgressDao.getUserProgress().first() ?: UserProgress()
        val completedLevels = userProgress.completedLevels.toMutableMap()
        val subjectLevels = completedLevels.getOrPut(subject) { emptyList() }.toMutableList()
        if (!subjectLevels.contains(level)) {
            subjectLevels.add(level)
        }
        completedLevels[subject] = subjectLevels
        userProgressDao.updateUserProgress(userProgress.copy(completedLevels = completedLevels))
    }

    override fun getUserProgress(): Flow<UserProgress?> {
        return userProgressDao.getUserProgress()
    }

    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>> {
        return sentenceDao.getSentencesByDifficulty(difficulty)
    }

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>> {
        return wordDao.getWordsByDifficulty(difficulty)
    }

    override fun getAllPunctuationQuestions(): Flow<List<PunctuationQuestion>> {
        return punctuationQuestionDao.getAllQuestions()
    }

    override fun getReadingComprehensionQuestions(level: Int): Flow<List<ReadingComprehensionQuestion>> {
        return readingComprehensionDao.getQuestionsByLevel(level)
    }
}