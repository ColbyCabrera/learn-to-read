package com.example.readingfoundations.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingfoundations.ui.screens.home.HomeScreen
import com.example.readingfoundations.ui.screens.level_selection.LevelSelectionScreen
import com.example.readingfoundations.ui.screens.phonetics.PhoneticsScreen
import com.example.readingfoundations.ui.screens.punctuation.PunctuationPracticeScreen
import com.example.readingfoundations.ui.screens.punctuation.QuizCompleteScreen
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
        composable("punctuation") { PunctuationPracticeScreen(navController) }
        composable("settings") { SettingsScreen(navController) }

        // Level Selection
        composable(
            "level_selection/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) {
            LevelSelectionScreen(navController = navController)
        }

        // Reading Screens with Level
        composable(
            "word_reading/{levelNumber}",
            arguments = listOf(navArgument("levelNumber") { type = NavType.IntType })
        ) {
            WordReadingScreen(navController = navController)
        }
        composable(
            "sentence_reading/{levelNumber}",
            arguments = listOf(navArgument("levelNumber") { type = NavType.IntType })
        ) {
            SentenceReadingScreen(navController = navController)
        }

        // Level Complete
        composable(
            "level_complete/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 0
            LevelCompleteScreen(navController = navController, level = level)
        }

        // Quiz Complete
        composable(
            "quiz_complete/{score}/{totalQuestions}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            QuizCompleteScreen(
                navController = navController,
                score = score,
                totalQuestions = totalQuestions
            )
        }
    }
}