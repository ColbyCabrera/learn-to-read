package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.UserProgressDao
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface UnitRepository {
    fun getUnits(): Flow<List<Unit>>
    suspend fun updateProgress(subject: String, level: Int)
    fun getUserProgress(): Flow<UserProgress?>
    fun getSentencesByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Sentence>>
    fun getWordsByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Word>>
    fun getAllPunctuationQuestions(): Flow<List<com.example.readingfoundations.data.models.PunctuationQuestion>>
}

@Singleton
class UnitRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val phonemeDao: com.example.readingfoundations.data.local.PhonemeDao,
    private val wordDao: com.example.readingfoundations.data.local.WordDao,
    private val sentenceDao: com.example.readingfoundations.data.local.SentenceDao,
    private val punctuationQuestionDao: com.example.readingfoundations.data.local.PunctuationQuestionDao
) : UnitRepository {

    override fun getUnits(): Flow<List<Unit>> {
        return combine(
            userProgressDao.getUserProgress(),
            phonemeDao.getHighestLevel(),
            wordDao.getHighestDifficulty(),
            sentenceDao.getHighestDifficulty(),
            punctuationQuestionDao.getHighestLevel()
        ) { userProgress, maxPhonemeLevel, maxWordLevel, maxSentenceLevel, maxPunctuationLevel ->
            val minLevels = minOf(maxPhonemeLevel, maxWordLevel, maxSentenceLevel, maxPunctuationLevel)
            val totalUnits = (minLevels + 1) / 2

            (1..totalUnits).map { unitId ->
                val levels = mutableListOf<Level>()
                val subjects = listOf("Phonetics", "Word Building", "Sentence Reading", "Punctuation")
                for (subject in subjects) {
                    for (i in 1..2) {
                        val levelNumber = (unitId - 1) * 2 + i
                        val isCompleted = userProgress?.completedLevels?.get(subject)?.contains(levelNumber) ?: false
                        levels.add(Level(subject, levelNumber, isCompleted))
                    }
                }
                val completedCount = levels.count { it.isCompleted }
                val progress = if (levels.isNotEmpty()) completedCount.toFloat() / levels.size else 0f
                Unit(unitId, levels, progress)
            }
        }
    }

    override suspend fun updateProgress(subject: String, level: Int) {
        val userProgress = userProgressDao.getUserProgressSync() ?: UserProgress()
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

    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Sentence>> {
        return sentenceDao.getSentencesByDifficulty(difficulty)
    }

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Word>> {
        return wordDao.getWordsByDifficulty(difficulty)
    }

    override fun getAllPunctuationQuestions(): Flow<List<com.example.readingfoundations.data.models.PunctuationQuestion>> {
        return punctuationQuestionDao.getAllQuestions()
    }
}