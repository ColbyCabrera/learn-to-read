package com.example.readingfoundations.data

import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.QuizQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides quiz questions from the local database.
 */
interface QuizRepository {
    /**
     * Get a list of mixed quiz questions for a specific level.
     */
    fun getQuizQuestions(level: Level): Flow<List<QuizQuestion>>
}