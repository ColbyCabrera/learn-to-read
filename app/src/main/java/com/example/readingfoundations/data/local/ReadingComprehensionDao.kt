package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionText
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingComprehensionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTexts(texts: List<ReadingComprehensionText>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuestions(questions: List<ReadingComprehensionQuestion>)

    @Query("SELECT * FROM reading_comprehension_texts WHERE level = :level")
    fun getTextsByLevel(level: Int): Flow<List<ReadingComprehensionText>>

    @Query("SELECT * FROM reading_comprehension_questions WHERE textId = :textId")
    fun getQuestionsForText(textId: Int): Flow<List<ReadingComprehensionQuestion>>

    @Query("SELECT COALESCE(MAX(level), 0) FROM reading_comprehension_texts")
    fun getHighestLevel(): Flow<Int>

    @Query("SELECT q.* FROM reading_comprehension_questions q INNER JOIN reading_comprehension_texts t ON q.textId = t.id WHERE t.level = :level")
    fun getQuestionsByLevel(level: Int): Flow<List<ReadingComprehensionQuestion>>
}