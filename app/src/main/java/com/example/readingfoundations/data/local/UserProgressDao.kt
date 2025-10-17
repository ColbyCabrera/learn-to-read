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

    /**
     * Observes the UserProgress row with id = 1 and emits updates when it changes.
     *
     * @return A Flow that emits the UserProgress for id = 1, or `null` if no row exists.
     */
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>

    /**
     * Fetches the user progress record with id = 1 from the database.
     *
     * @return The `UserProgress` for id = 1, or `null` if no such record exists.
     */
    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressSync(): UserProgress?
}