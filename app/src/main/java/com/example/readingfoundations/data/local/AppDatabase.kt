package com.example.readingfoundations.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.readingfoundations.data.models.Phoneme
import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionQuestion
import com.example.readingfoundations.data.models.ReadingComprehensionText
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Word::class, Sentence::class, UserProgress::class, Phoneme::class, PunctuationQuestion::class, ReadingComprehensionText::class, ReadingComprehensionQuestion::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun sentenceDao(): SentenceDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun punctuationQuestionDao(): PunctuationQuestionDao
    abstract fun phonemeDao(): PhonemeDao
    abstract fun readingComprehensionDao(): ReadingComprehensionDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "reading_foundations_database")
                    .fallbackToDestructiveMigration(true) // Handle schema changes by destroying old data
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate the database on creation
                            CoroutineScope(Dispatchers.IO).launch {
                                Instance?.let { database ->
                                    database.wordDao().insertAll(PrepopulateData.words)
                                    database.sentenceDao().insertAll(PrepopulateData.sentences)
                                    database.punctuationQuestionDao().insertAll(PrepopulateData.punctuationQuestions)
                                    database.phonemeDao().insertAll(PrepopulateData.phonemes)
                                    database.readingComprehensionDao().insertAllTexts(PrepopulateData.readingComprehensionTexts)
                                    database.readingComprehensionDao().insertAllQuestions(PrepopulateData.readingComprehensionQuestions)
                                    // Initialize user progress
                                    database.userProgressDao().updateUserProgress(UserProgress())
                                }
                            }
                        }
                    })
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `punctuation_questions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `correctAnswer` TEXT NOT NULL, `options` TEXT)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `reading_comprehension_texts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `level` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE `reading_comprehension_questions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `textId` INTEGER NOT NULL, `questionText` TEXT NOT NULL, `correctAnswer` TEXT NOT NULL, `questionType` TEXT NOT NULL, FOREIGN KEY(`textId`) REFERENCES `reading_comprehension_texts`(`id`) ON DELETE CASCADE)")

                // Pre-populate the new tables
                db.execSQL("INSERT INTO reading_comprehension_texts (id, text, level) VALUES (1, 'The dog chased the ball.', 0)")
                db.execSQL("INSERT INTO reading_comprehension_texts (id, text, level) VALUES (2, 'A girl has a red kite. She flies the kite high.', 1)")
                db.execSQL("INSERT INTO reading_comprehension_texts (id, text, level) VALUES (3, 'The sun is bright today. A boy goes to the park to play. He sees a blue bird in a tree. The bird sings a happy song.', 2)")
                db.execSQL("INSERT INTO reading_comprehension_texts (id, text, level) VALUES (4, 'The little gray mouse quietly crept across the kitchen floor. He was looking for a tasty crumb of cheese. Suddenly, he heard a loud noise and felt scared. He quickly ran back to his small hole in the wall.', 3)")
                db.execSQL("INSERT INTO reading_comprehension_texts (id, text, level) VALUES (5, 'Once, there was a curious cat named Leo. Leo loved to explore the garden behind his house. One sunny afternoon, he saw a bright blue butterfly. He decided to follow it.\\n\\nThe butterfly fluttered over a tall fence. Leo, determined to keep up, took a big leap and landed softly on the other side. He found himself in a new, exciting place he had never seen before. It was a meadow full of colorful wildflowers.\\n\\nLeo forgot all about the butterfly and spent the rest of the day happily rolling in the flowers and chasing grasshoppers. He knew he had found his new favorite place to play.', 4)")

                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (1, 1, 'Who chased the ball?', 'The dog', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (2, 1, 'What did the dog chase?', 'The ball', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (3, 2, 'What does the girl have?', 'A red kite', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (4, 2, 'Who flies the kite?', 'She', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (5, 3, 'Where does the boy go to play?', 'The park', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (6, 3, 'What does the boy see in the tree?', 'A blue bird', 'LITERAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (7, 3, 'What is this paragraph mostly about?', 'A boy playing in the park', 'MAIN_IDEA')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (8, 4, 'How did the mouse feel when he heard a noise?', 'Scared', 'INFERENTIAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (9, 4, 'The text says the mouse ''quietly crept''. What was the mouse doing?', 'Moving silently', 'VOCABULARY_IN_CONTEXT')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (10, 4, 'Why did the mouse run back to his hole?', 'Because he was scared', 'INFERENTIAL')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (11, 5, 'What happened after Leo jumped over the fence?', 'He found a meadow', 'SEQUENCING')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (12, 5, 'What is the main idea of the story?', 'A cat discovers a new favorite place', 'MAIN_IDEA')")
                db.execSQL("INSERT INTO reading_comprehension_questions (id, textId, questionText, correctAnswer, questionType) VALUES (13, 5, 'Based on the story, what do you think Leo will do tomorrow?', 'Go back to the meadow', 'PREDICTIVE')")
            }
        }
    }
}