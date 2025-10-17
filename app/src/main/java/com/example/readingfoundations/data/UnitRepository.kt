package com.example.readingfoundations.data

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
    private val phonemeDao: com.example.readingfoundations.data.local.PhonemeDao,
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
            progressFlow,
            phonemeLevelFlow,
            wordLevelFlow,
            sentenceLevelFlow,
            punctuationLevelFlow
        ) { userProgress, maxPhonemeLevel, maxWordLevel, maxSentenceLevel, maxPunctuationLevel ->
            Triple(userProgress, maxPhonemeLevel, listOf(maxWordLevel, maxSentenceLevel, maxPunctuationLevel))
        }.combine(readingComprehensionLevelFlow) { (userProgress, maxPhonemeLevel, otherLevels), maxReadingComprehensionLevel ->
            val allLevels = otherLevels + maxReadingComprehensionLevel
            val minLevels = (allLevels + maxPhonemeLevel).minOrNull() ?: 0
            val totalUnits = (minLevels + 1) / 2

            (1..totalUnits).map { unitId ->
                val levels = mutableListOf<Level>()
                val subjects = mapOf(
                    Subjects.PHONETICS to maxPhonemeLevel,
                    Subjects.WORD_BUILDING to allLevels[0],
                    Subjects.SENTENCE_READING to allLevels[1],
                    Subjects.PUNCTUATION to allLevels[2],
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
        return readingComprehensionDao.getQuestionsForText(level)
    }
}