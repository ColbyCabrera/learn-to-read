package com.example.readingfoundations.data

import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.flow.Flow

interface PhonemeRepository {
    fun getAllPhonemes(): Flow<List<Phoneme>>
}