package com.example.readingfoundations.data.local

import com.example.readingfoundations.data.DataSource
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val wordDao: WordDao,
    private val sentenceDao: SentenceDao,
    private val userProgressDao: UserProgressDao
) : DataSource {

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>> {
        return wordDao.getWordsByDifficulty(difficulty)
    }

    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>> {
        return sentenceDao.getSentencesByDifficulty(difficulty)
    }

    override fun getUserProgress(): Flow<UserProgress?> {
        return userProgressDao.getUserProgress()
    }

    override suspend fun updateUserProgress(userProgress: UserProgress) {
        userProgressDao.updateUserProgress(userProgress)
    }
}