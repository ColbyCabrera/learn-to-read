package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val wordLevelsProgress: Map<Int, Int> = emptyMap(),
    val sentenceLevelsProgress: Map<Int, Int> = emptyMap(),
    val currentWordLevelInProgress: Int = 0,
    val currentSentenceLevelInProgress: Int = 0
)