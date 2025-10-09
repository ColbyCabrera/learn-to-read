package com.example.readingfoundations.data

import com.example.readingfoundations.R

enum class ContentType(val title: String, val icon: Int) {
    PHONETICS("Phonetics", R.drawable.ic_phonetics),
    WORD_BUILDING("Word Building", R.drawable.ic_word_building),
    SENTENCE_BUILDING("Sentence Building", R.drawable.ic_sentence_reading)
}

data class Level(
    val levelNumber: Int,
    val difficulty: Int // Maps to the difficulty/level in PrepopulateData
)

data class Unit(
    val id: String,
    val title: String,
    val levels: Map<ContentType, List<Level>>
)

object Curriculum {
    val units = listOf(
        Unit(
            id = "unit_1",
            title = "Unit 1",
            levels = mapOf(
                ContentType.PHONETICS to listOf(Level(1, 1), Level(2, 2)),
                ContentType.WORD_BUILDING to listOf(Level(1, 1), Level(2, 2)),
                ContentType.SENTENCE_BUILDING to listOf(Level(1, 1), Level(2, 2))
            )
        ),
        Unit(
            id = "unit_2",
            title = "Unit 2",
            levels = mapOf(
                ContentType.PHONETICS to listOf(Level(3, 3), Level(4, 4)),
                ContentType.WORD_BUILDING to listOf(Level(3, 3), Level(4, 4)),
                ContentType.SENTENCE_BUILDING to listOf(Level(3, 3), Level(4, 4))
            )
        ),
        Unit(
            id = "unit_3",
            title = "Unit 3",
            levels = mapOf(
                ContentType.WORD_BUILDING to listOf(Level(5, 5), Level(6, 6)),
                ContentType.SENTENCE_BUILDING to listOf(Level(5, 5), Level(6, 6))
            )
        ),
        Unit(
            id = "unit_4",
            title = "Unit 4",
            levels = mapOf(
                ContentType.WORD_BUILDING to listOf(Level(7, 7), Level(8, 8)),
                ContentType.SENTENCE_BUILDING to listOf(Level(7, 7), Level(8, 8))
            )
        ),
        Unit(
            id = "unit_5",
            title = "Unit 5",
            levels = mapOf(
                ContentType.WORD_BUILDING to listOf(Level(9, 9), Level(10, 10)),
                ContentType.SENTENCE_BUILDING to listOf(Level(9, 9), Level(10, 10))
            )
        )
    )
}