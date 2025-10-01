package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.LocalDataSource
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import com.example.readingfoundations.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : DataSource {

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>> {
        // For now, we only fetch from the local data source.
        // In the future, logic can be added here to fetch from the remote data source
        // and cache the results locally.
        return localDataSource.getWordsByDifficulty(difficulty)
    }

    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>> {
        return localDataSource.getSentencesByDifficulty(difficulty)
    }

    override fun getUserProgress(): Flow<UserProgress?> {
        return localDataSource.getUserProgress()
    }

    override suspend fun updateUserProgress(userProgress: UserProgress) {
        localDataSource.updateUserProgress(userProgress)
    }
}