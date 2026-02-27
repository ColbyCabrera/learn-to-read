package com.example.readingfoundations.data

import androidx.annotation.StringRes
import com.example.readingfoundations.R

object Subjects {
    const val PHONETICS = "Phonetics"
    const val WORD_BUILDING = "Word Building"
    const val SENTENCE_READING = "Sentence Reading"
    const val PUNCTUATION = "Punctuation"
    const val READING_COMPREHENSION = "Reading Comprehension"

    val ALL = listOf(PHONETICS, WORD_BUILDING, SENTENCE_READING, PUNCTUATION, READING_COMPREHENSION)

    @StringRes
    fun getTitleRes(subject: String): Int {
        return when (subject) {
            PHONETICS -> R.string.phonetics
            WORD_BUILDING -> R.string.word_building
            SENTENCE_READING -> R.string.sentence_reading
            PUNCTUATION -> R.string.punctuation
            READING_COMPREHENSION -> R.string.reading_comprehension
            else -> R.string.subjects
        }
    }
}