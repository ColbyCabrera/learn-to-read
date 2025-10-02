package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.readingfoundations.data.local.Converters

@Entity(tableName = "punctuation_questions")
@TypeConverters(Converters::class)
data class PunctuationQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val difficulty: Int
)