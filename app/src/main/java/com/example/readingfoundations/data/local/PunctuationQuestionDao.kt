package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readingfoundations.data.models.PunctuationQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface PunctuationQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<PunctuationQuestion>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: PunctuationQuestion)

    @Query("SELECT * FROM punctuation_questions")
    fun getAllQuestions(): Flow<List<PunctuationQuestion>>

    @Query("SELECT COALESCE(MAX(level), 0) FROM punctuation_questions")
    fun getHighestLevel(): Flow<Int>
}