package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readingfoundations.data.models.Sentence
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sentences: List<Sentence>)

    @Query("SELECT * FROM sentences WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT 5")
    fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>>

    @Query("SELECT COALESCE(MAX(difficulty), 0) FROM sentences")
    fun getHighestDifficulty(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sentences")
    suspend fun getCount(): Int

    @Query("SELECT * FROM sentences WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    fun getRandomSentencesByDifficulty(difficulty: Int, limit: Int): Flow<List<Sentence>>
}