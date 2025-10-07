package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<Word>)

    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT 10")
    fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>>

    @Query("SELECT COALESCE(MAX(difficulty), 0) FROM words")
    fun getHighestDifficulty(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int

    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    fun getRandomWordsByDifficulty(difficulty: Int, limit: Int): Flow<List<Word>>
}