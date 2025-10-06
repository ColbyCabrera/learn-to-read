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
            }
        }
    }
}