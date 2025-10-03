package com.example.readingfoundations.data

import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.flow.Flow

interface DataSource {

    fun getAllPunctuationQuestions(): Flow<List<PunctuationQuestion>>

    fun getWordsByDifficulty(difficulty: Int): Flow<List<Word>>

    fun getSentencesByDifficulty(difficulty: Int): Flow<List<Sentence>>

    fun getWordLevelCount(): Flow<Int>

    fun getSentenceLevelCount(): Flow<Int>

    fun getUserProgress(): Flow<UserProgress?>

    suspend fun updateUserProgress(userProgress: UserProgress)
}