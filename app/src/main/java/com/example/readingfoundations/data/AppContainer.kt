package com.example.readingfoundations.data

import com.example.readingfoundations.data.local.LocalDataSource
import com.example.readingfoundations.data.remote.RemoteDataSource

interface AppContainer {
    val localDataSource: LocalDataSource
    val remoteDataSource: RemoteDataSource
    val appRepository: AppRepository
    val userPreferencesRepository: UserPreferencesRepository
}