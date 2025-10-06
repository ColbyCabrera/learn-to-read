package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_comprehension_texts")
data class ReadingComprehensionText(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val level: Int
)