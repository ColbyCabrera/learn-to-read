package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentences")
data class Sentence(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val difficulty: Int
)