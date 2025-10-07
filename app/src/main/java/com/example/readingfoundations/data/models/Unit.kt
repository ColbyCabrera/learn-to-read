package com.example.readingfoundations.data.models

/**
 * Represents a single level within a unit, specifying the number of questions for each category.
 *
 * @property level The numeric identifier for the level (e.g., 1, 2, 3).
 * @property phonemeCount The number of phoneme questions in this level.
 * @property wordCount The number of word-building questions in this level.
 * @property sentenceCount The number of sentence-reading questions in this level.
 * @property punctuationCount The number of punctuation questions in this level.
 */
data class Level(
    val level: Int,
    val phonemeCount: Int,
    val wordCount: Int,
    val sentenceCount: Int,
    val punctuationCount: Int
)

/**
 * Represents a learning unit, which is composed of a series of levels.
 *
 * @property title The title of the unit (e.g., "Foundations 1").
 * @property levels A list of [Level]s that make up the unit.
 */
data class Unit(
    val title: String,
    val levels: List<Level>
)

/**
 * A predefined list of all learning units available in the app.
 * This static data defines the curriculum structure.
 */
val allUnits = listOf(
    Unit(
        title = "Foundations 1",
        levels = listOf(
            Level(level = 1, phonemeCount = 5, wordCount = 5, sentenceCount = 2, punctuationCount = 1),
            Level(level = 2, phonemeCount = 5, wordCount = 5, sentenceCount = 3, punctuationCount = 1),
            Level(level = 3, phonemeCount = 5, wordCount = 5, sentenceCount = 4, punctuationCount = 2)
        )
    ),
    Unit(
        title = "Foundations 2",
        levels = listOf(
            Level(level = 4, phonemeCount = 4, wordCount = 6, sentenceCount = 4, punctuationCount = 2),
            Level(level = 5, phonemeCount = 4, wordCount = 6, sentenceCount = 5, punctuationCount = 3),
            Level(level = 6, phonemeCount = 4, wordCount = 6, sentenceCount = 6, punctuationCount = 3)
        )
    ),
    Unit(
        title = "Advanced Skills",
        levels = listOf(
            Level(level = 7, phonemeCount = 3, wordCount = 7, sentenceCount = 6, punctuationCount = 4),
            Level(level = 8, phonemeCount = 3, wordCount = 7, sentenceCount = 7, punctuationCount = 4),
            Level(level = 9, phonemeCount = 2, wordCount = 8, sentenceCount = 8, punctuationCount = 5),
            Level(level = 10, phonemeCount = 1, wordCount = 8, sentenceCount = 9, punctuationCount = 6)
        )
    )
)