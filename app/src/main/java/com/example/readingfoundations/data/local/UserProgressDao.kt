package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.readingfoundations.data.models.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Upsert
    suspend fun updateUserProgress(userProgress: UserProgress)

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>
}