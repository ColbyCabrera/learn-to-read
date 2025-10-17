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
    /**
 * Emits the current list of units, each populated with its levels and overall completion progress.
 *
 * Each emitted list reflects the latest data and user progress; every Unit contains its Levels
 * (two levels per subject) and a progress value representing the fraction of completed levels
 * for that unit (0.0..1.0).
 *
 * @return A Flow that emits lists of Unit with levels and a per-unit completion fraction. 
 */
fun getUnits(): Flow<List<Unit>>
    /**
 * Marks the given level as completed for the specified subject in the user's progress.
 *
 * If no user progress exists, a new progress record is created. The level is added to the subject's
 * completed levels only if it is not already present, and the updated progress is persisted.
 *
 * @param subject The subject identifier (for example "Phonetics", "Word Building", "Sentence Reading", or "Punctuation").
 * @param level The level number to mark as completed.
 */
suspend fun updateProgress(subject: String, level: Int)
    /**
 * Observes stored user progress for the app.
 *
 * @return The latest `UserProgress` if available, or `null` when no progress has been recorded.
 */
fun getUserProgress(): Flow<UserProgress?>
    /**
 * Retrieve sentences that match a given difficulty level.
 *
 * @param difficulty Difficulty level to filter sentences; higher values indicate greater difficulty.
 * @return Lists of Sentence objects that match the specified difficulty.
 */
fun getSentencesByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Sentence>>
    /**
 * Emits words filtered by the given difficulty level.
 *
 * @param difficulty The difficulty level to filter words by.
 * @return Lists of Word matching the specified difficulty level.
 */
fun getWordsByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Word>>
    /**
 * Provides all punctuation questions.
 *
 * @return A Flow emitting lists of all stored com.example.readingfoundations.data.models.PunctuationQuestion objects.
 */
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

    /**
     * Emits a list of units, each containing two levels per subject with completion flags and a per-unit progress value.
     *
     * The emitted units are computed from the current user progress and the available max levels/difficulties; each unit's
     * progress is the fraction of its levels that are completed.
     *
     * @return A Flow that emits a List of Unit where each Unit includes its id, a list of Level(subject, levelNumber, isCompleted),
     * and a progress value (completed levels divided by total levels).
     */
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

    /**
     * Marks the specified level as completed for the given subject in the persisted user progress.
     *
     * @param subject The subject identifier (for example "Phonetics", "Word Building") under which the level will be recorded.
     * @param level The level number to mark as completed.
     */
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

    /**
     * Observes the stored user progress.
     *
     * @return `UserProgress` if present, `null` otherwise.
     */
    override fun getUserProgress(): Flow<UserProgress?> {
        return userProgressDao.getUserProgress()
    }

    /**
     * Emits sentences filtered by the specified difficulty level.
     *
     * @param difficulty Difficulty level to filter sentences by.
     * @return Lists of Sentence objects that match the specified difficulty. 
     */
    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Sentence>> {
        return sentenceDao.getSentencesByDifficulty(difficulty)
    }

    /**
     * Retrieves words filtered by the specified difficulty.
     *
     * @param difficulty The difficulty level used to filter words.
     * @return Lists of words that match the given difficulty.
     */
    override fun getWordsByDifficulty(difficulty: Int): Flow<List<com.example.readingfoundations.data.models.Word>> {
        return wordDao.getWordsByDifficulty(difficulty)
    }

    /**
     * Provides a stream of all punctuation questions.
     *
     * @return A Flow that emits lists of PunctuationQuestion representing all stored punctuation questions.
     */
    override fun getAllPunctuationQuestions(): Flow<List<com.example.readingfoundations.data.models.PunctuationQuestion>> {
        return punctuationQuestionDao.getAllQuestions()
    }
}