package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val completedLevels: Map<String, List<Int>> = emptyMap()
)