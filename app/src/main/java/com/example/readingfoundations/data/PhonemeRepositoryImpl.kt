package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.flow.Flow

class PhonemeRepositoryImpl(private val phonemeDao: PhonemeDao) : PhonemeRepository {
    override fun getAllPhonemes(): Flow<List<Phoneme>> = phonemeDao.getAllPhonemes()
}