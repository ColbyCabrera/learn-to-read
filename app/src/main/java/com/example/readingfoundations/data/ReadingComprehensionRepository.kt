package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.ReadingComprehensionDao
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionText
import kotlinx.coroutines.flow.Flow

interface ReadingComprehensionRepository {
    fun getTextsByLevel(level: Int): Flow<List<ReadingComprehensionText>>
    fun getQuestionsForText(textId: Int): Flow<List<ReadingComprehensionQuestion>>
    suspend fun insertAllTexts(texts: List<ReadingComprehensionText>)
    suspend fun insertAllQuestions(questions: List<ReadingComprehensionQuestion>)
}

class ReadingComprehensionRepositoryImpl(private val readingComprehensionDao: ReadingComprehensionDao) : ReadingComprehensionRepository {
    override fun getTextsByLevel(level: Int): Flow<List<ReadingComprehensionText>> =
        readingComprehensionDao.getTextsByLevel(level)

    override fun getQuestionsForText(textId: Int): Flow<List<ReadingComprehensionQuestion>> =
        readingComprehensionDao.getQuestionsForText(textId)

    override suspend fun insertAllTexts(texts: List<ReadingComprehensionText>) {
        readingComprehensionDao.insertAllTexts(texts)
    }

    override suspend fun insertAllQuestions(questions: List<ReadingComprehensionQuestion>) {
        readingComprehensionDao.insertAllQuestions(questions)
    }
}