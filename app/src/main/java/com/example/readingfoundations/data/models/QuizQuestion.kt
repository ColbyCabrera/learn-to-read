package com.example.readingfoundations.data.models

/**
 * A sealed interface representing a question in a quiz.
 * This allows for different types of questions (phoneme, word, etc.) to be used interchangeably in a quiz.
 */
sealed interface QuizQuestion {
    data class PhonemeQuestion(val phoneme: Phoneme) : QuizQuestion
    data class WordQuestion(val word: Word) : QuizQuestion
    data class SentenceQuestion(val sentence: Sentence) : QuizQuestion
    data class PunctuationQuestionItem(val punctuationQuestion: PunctuationQuestion) : QuizQuestion
}