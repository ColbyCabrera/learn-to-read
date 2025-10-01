package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val lastWordLevelCompleted: Int = 0,
    val lastSentenceLevelCompleted: Int = 0
)