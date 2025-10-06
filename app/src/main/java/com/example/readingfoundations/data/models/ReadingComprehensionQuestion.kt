package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_comprehension_questions",
    foreignKeys = [
        ForeignKey(
            entity = ReadingComprehensionText::class,
            parentColumns = ["id"],
            childColumns = ["textId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReadingComprehensionQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val textId: Int,
    val questionText: String,
    val correctAnswer: String,
    val questionType: String // e.g., "Literal", "Inferential", "Vocabulary"
)