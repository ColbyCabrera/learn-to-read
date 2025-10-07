package com.example.readingfoundations.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.readingfoundations.ReadingFoundationsApp
import com.example.readingfoundations.ui.screens.home.HomeViewModel
import com.example.readingfoundations.ui.screens.phonetics.PhoneticsViewModel
import com.example.readingfoundations.ui.screens.punctuation.PunctuationViewModel
import com.example.readingfoundations.ui.screens.reading_comprehension.ReadingComprehensionViewModel
import com.example.readingfoundations.ui.screens.reading_sentence.SentenceReadingViewModel
import com.example.readingfoundations.ui.screens.reading_word.WordReadingViewModel
import com.example.readingfoundations.ui.screens.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                readingFoundationsApplication().container.appRepository
            )
        }
        initializer {
            PhoneticsViewModel()
        }
        initializer {
            PunctuationViewModel(
                readingFoundationsApplication().container.appRepository
            )
        }
        initializer {
            val savedStateHandle = createSavedStateHandle()
            WordReadingViewModel(
                readingFoundationsApplication().container.appRepository,
                savedStateHandle
            )
        }
        initializer {
            val savedStateHandle = createSavedStateHandle()
            SentenceReadingViewModel(
                readingFoundationsApplication().container.appRepository,
                savedStateHandle
            )
        }
        initializer {
            SettingsViewModel(
                readingFoundationsApplication().container.userPreferencesRepository
            )
        }
        initializer {
            ReadingComprehensionViewModel(
                readingFoundationsApplication().container.readingComprehensionRepository
            )
        }
    }
}

fun CreationExtras.readingFoundationsApplication(): ReadingFoundationsApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ReadingFoundationsApp)