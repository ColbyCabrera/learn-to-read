package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "punctuation_questions")
data class PunctuationQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val correctAnswer: String
)
