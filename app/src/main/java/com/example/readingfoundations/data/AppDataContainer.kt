package com.example.readingfoundations.data

import android.content.Context
import com.example.readingfoundations.data.local.AppDatabase
import com.example.readingfoundations.data.local.LocalDataSource
import com.example.readingfoundations.data.remote.RemoteDataSource

class AppDataContainer(private val context: Context) : AppContainer {

    override val localDataSource: LocalDataSource by lazy {
        LocalDataSource(
            AppDatabase.getDatabase(context).wordDao(),
            AppDatabase.getDatabase(context).sentenceDao(),
            AppDatabase.getDatabase(context).userProgressDao(),
            AppDatabase.getDatabase(context).punctuationQuestionDao(),
            AppDatabase.getDatabase(context).phonemeDao()
        )
    }

    override val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource()
    }

    override val appRepository: AppRepository by lazy {
        AppRepository(localDataSource, remoteDataSource)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val readingComprehensionRepository: ReadingComprehensionRepository by lazy {
        ReadingComprehensionRepositoryImpl(AppDatabase.getDatabase(context).readingComprehensionDao())
    }

    override val unitRepository: UnitRepository by lazy {
        UnitRepositoryImpl(
            AppDatabase.getDatabase(context).userProgressDao(),
            AppDatabase.getDatabase(context).phonemeDao(),
            AppDatabase.getDatabase(context).wordDao(),
            AppDatabase.getDatabase(context).sentenceDao(),
            AppDatabase.getDatabase(context).punctuationQuestionDao(),
            AppDatabase.getDatabase(context).readingComprehensionDao()
        )
    }
}