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

    /**
     * Observes all punctuation question records in the local database.
     *
     * @return A Flow that emits the current list of PunctuationQuestion entities in the `punctuation_questions` table.
     */
    @Query("SELECT * FROM punctuation_questions")
    fun getAllQuestions(): Flow<List<PunctuationQuestion>>

    /**
     * Retrieves the highest `level` value present in the `punctuation_questions` table, defaulting to 0 when the table is empty.
     *
     * @return The maximum `level` as an `Int`; `0` if no rows exist.
     */
    @Query("SELECT COALESCE(MAX(level), 0) FROM punctuation_questions")
    fun getHighestLevel(): Flow<Int>
}