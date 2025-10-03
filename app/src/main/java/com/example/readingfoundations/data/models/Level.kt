package com.example.readingfoundations.data.models

data class Level(
    val levelNumber: Int,
    val totalQuestions: Int,
    var questionsCorrect: Int = 0,
    val isCompleted: Boolean = false
)