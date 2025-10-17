package com.example.readingfoundations.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readingfoundations.data.models.Phoneme
import kotlinx.coroutines.flow.Flow

@Dao
interface PhonemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phonemes: List<Phoneme>)

    @Query("SELECT * FROM phonemes ORDER BY level, id")
    fun getAllPhonemes(): Flow<List<Phoneme>>

    @Query("SELECT * FROM phonemes WHERE level = :level ORDER BY id")
    fun getPhonemesByLevel(level: Int): Flow<List<Phoneme>>

    /**
     * Observes phonemes that belong to the specified category.
     *
     * @param category The phoneme category to filter by (exact match).
     * @return Lists of phonemes in the specified category, ordered by `level` then `id`.
     */
    @Query("SELECT * FROM phonemes WHERE category = :category ORDER BY level, id")
    fun getPhonemesByCategory(category: String): Flow<List<Phoneme>>

    /**
     * Provides the current highest phoneme level in the database.
     *
     * @return The highest phoneme level as an Int; 0 if the table contains no phonemes.
     */
    @Query("SELECT COALESCE(MAX(level), 0) FROM phonemes")
    fun getHighestLevel(): Flow<Int>
}