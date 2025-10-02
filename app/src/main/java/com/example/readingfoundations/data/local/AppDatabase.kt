package com.example.readingfoundations.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.data.models.UserProgress
import com.example.readingfoundations.data.models.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Word::class, Sentence::class, UserProgress::class, PunctuationQuestion::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun sentenceDao(): SentenceDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun punctuationQuestionDao(): PunctuationQuestionDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "reading_foundations_database")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate the database on creation
                            CoroutineScope(Dispatchers.IO).launch {
                                Instance?.let { database ->
                                    database.wordDao().insertAll(PrepopulateData.words)
                                    database.sentenceDao().insertAll(PrepopulateData.sentences)
                                    database.punctuationQuestionDao().insertAll(PrepopulateData.punctuationQuestions)
                                    // Initialize user progress
                                    database.userProgressDao().updateUserProgress(UserProgress())
                                }
                            }
                        }
                    })
                    .addMigrations(MIGRATION_4_5)
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE punctuation_questions ADD COLUMN options TEXT")
            }
        }
    }
}