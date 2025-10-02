package com.example.readingfoundations.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingfoundations.ui.screens.home.HomeScreen
import com.example.readingfoundations.ui.screens.phonetics.PhoneticsScreen
import com.example.readingfoundations.ui.screens.punctuation.PunctuationScreen
import com.example.readingfoundations.ui.screens.reading_sentence.SentenceReadingScreen
import com.example.readingfoundations.ui.screens.reading_word.LevelCompleteScreen
import com.example.readingfoundations.ui.screens.reading_word.WordReadingScreen
import com.example.readingfoundations.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("phonetics") { PhoneticsScreen(navController) }
        composable("word_building") { WordReadingScreen(navController) }
        composable("sentence_reading") { SentenceReadingScreen(navController) }
        composable("punctuation") { PunctuationScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable(
            "level_complete/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 0
            LevelCompleteScreen(navController = navController, level = level)
        }
    }
}