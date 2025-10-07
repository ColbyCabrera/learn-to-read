package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.PhonemeDao
import com.example.readingfoundations.data.local.PunctuationQuestionDao
import com.example.readingfoundations.data.local.SentenceDao
import com.example.readingfoundations.data.local.WordDao
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.QuizQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * An offline-first implementation of the [QuizRepository].
 * This repository fetches quiz questions from the local Room database.
 */
class OfflineQuizRepository(
    private val phonemeDao: PhonemeDao,
    private val wordDao: WordDao,
    private val sentenceDao: SentenceDao,
    private val punctuationQuestionDao: PunctuationQuestionDao
) : QuizRepository {

    /**
     * Retrieves a combined and shuffled list of quiz questions for a given [Level].
     *
     * This function fetches a specified number of random questions for each category
     * (phonemes, words, sentences, punctuation) based on the counts defined in the [level] parameter.
     * It then combines these lists into a single list of [QuizQuestion]s and shuffles them
     * to provide a varied quiz experience.
     *
     * @param level The [Level] object that defines the difficulty and composition of the quiz.
     * @return A [Flow] emitting a shuffled list of [QuizQuestion]s.
     */
    override fun getQuizQuestions(level: Level): Flow<List<QuizQuestion>> {
        val phonemeFlow = phonemeDao.getRandomPhonemesByLevel(level.level, level.phonemeCount)
        val wordFlow = wordDao.getRandomWordsByDifficulty(level.level, level.wordCount)
        val sentenceFlow = sentenceDao.getRandomSentencesByDifficulty(level.level, level.sentenceCount)
        val punctuationFlow = punctuationQuestionDao.getRandomPunctuationQuestionsByDifficulty(level.level, level.punctuationCount)

        return combine(phonemeFlow, wordFlow, sentenceFlow, punctuationFlow) { phonemes, words, sentences, punctuationQuestions ->
            val combinedList = mutableListOf<QuizQuestion>()
            phonemes.map { combinedList.add(QuizQuestion.PhonemeQuestion(it)) }
            words.map { combinedList.add(QuizQuestion.WordQuestion(it)) }
            sentences.map { combinedList.add(QuizQuestion.SentenceQuestion(it)) }
            punctuationQuestions.map { combinedList.add(QuizQuestion.PunctuationQuestionItem(it)) }
            combinedList.shuffled()
        }
    }
}