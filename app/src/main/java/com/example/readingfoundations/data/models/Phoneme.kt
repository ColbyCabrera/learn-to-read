package com.example.readingfoundations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phonemes")
data class Phoneme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // The written letter or letters (e.g., "s", "sh", "a_e")
    val grapheme: String,
    // The pure sound for TTS to pronounce in isolation (e.g., "s", "sh", "ay")
    val sound: String,
    // A full descriptive phrase for TTS (e.g., "s as in sun")
    val ttsText: String,
    // An example word containing the phoneme
    val exampleWord: String,
    // The category for grouping (e.g., "CVC Phoneme", "Digraph", "Vowel Team")
    val category: String,
    // The curriculum level to control progression
    val level: Int
)