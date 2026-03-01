package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class PhonemeRepositoryTest {

    @Mock
    private lateinit var phonemeDao: PhonemeDao

    private lateinit var phonemeRepository: PhonemeRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        phonemeRepository = PhonemeRepositoryImpl(phonemeDao)
    }

    @Test
    fun `getAllPhonemes returns flow from dao`() = runTest {
        val phonemes = listOf(
            Phoneme(id = 1, grapheme = "a", sound = "a", ttsText = "a", exampleWord = "apple", category = "vowel", level = 1),
            Phoneme(id = 2, grapheme = "b", sound = "b", ttsText = "b", exampleWord = "ball", category = "consonant", level = 1)
        )
        `when`(phonemeDao.getAllPhonemes()).thenReturn(flowOf(phonemes))

        val result = phonemeRepository.getAllPhonemes().first()

        assertEquals(phonemes, result)
    }
}
