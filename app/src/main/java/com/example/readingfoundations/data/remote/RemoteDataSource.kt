package com.example.readingfoundations.data.remote

import com.example.readingfoundations.data.DataSource
import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
*/

/**
 * A placeholder remote data source that implements the DataSource interface.
 * This file contains commented-out boilerplate for Retrofit and Ktor to facilitate
 * future integration with a remote backend.
 */
class RemoteDataSource(/* private val ktorClient: HttpClient, private val retrofitService: ApiService */) : DataSource {

    override fun getAllPunctuationQuestions(): Flow<List<PunctuationQuestion>> {
        return flow { emit(emptyList()) }
    }

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>> {
        // Ktor Example:
        // return flow { emit(ktorClient.get("words/{difficulty}").body()) }

        // Retrofit Example:
        // return flow { emit(retrofitService.getWordsByDifficulty(difficulty)) }

        // Placeholder implementation
        return flow { emit(emptyList()) }
    }

    override fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>> {
        // Ktor aexample:
        // return flow { emit(ktorClient.get("sentences/{difficulty}").body()) }

        // Retrofit Example:
        // return flow { emit(retrofitService.getSentencesByDifficulty(difficulty)) }

        // Placeholder implementation
        return flow { emit(emptyList()) }
    }

    override fun getUserProgress(): Flow<UserProgress?> {
        // Ktor Example:
        // return flow { emit(ktorClient.get("user/progress").body()) }

        // Retrofit Example:
        // return flow { emit(retrofitService.getUserProgress()) }

        // Placeholder implementation
        return flow { emit(null) }
    }

    override suspend fun updateUserProgress(userProgress: UserProgress) {
        // Ktor Example:
        // ktorClient.post("user/progress") { setBody(userProgress) }

        // Retrofit Example:
        // retrofitService.updateUserProgress(userProgress)

        // Placeholder implementation
    }

    override fun getWordLevelCount(): Flow<Int> {
        return flow { emit(0) }
    }

    override fun getSentenceLevelCount(): Flow<Int> {
        return flow { emit(0) }
    }
}

/*
// Boilerplate for Retrofit ApiService
interface ApiService {
    @GET("words/{difficulty}")
    suspend fun getWordsByDifficulty(@Path("difficulty") difficulty: Int): List<Word>

    @GET("sentences/{difficulty}")
    suspend fun getSentencesByDifficulty(@Path("difficulty") difficulty: Int): List<Sentence>

    @GET("user/progress")
    suspend fun getUserProgress(): UserProgress

    @POST("user/progress")
    suspend fun updateUserProgress(@Body userProgress: UserProgress)
}
*/