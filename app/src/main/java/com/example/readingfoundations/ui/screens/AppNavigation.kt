package com.example.readingfoundations.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingfoundations.ui.screens.home.HomeScreen
import com.example.readingfoundations.ui.screens.phonetics.PhoneticsScreen
import com.example.readingfoundations.ui.screens.punctuation.PunctuationPracticeScreen
import com.example.readingfoundations.ui.screens.punctuation.QuizCompleteScreen
import com.example.readingfoundations.ui.screens.reading_sentence.SentenceReadingScreen
import com.example.readingfoundations.ui.screens.reading_word.LevelCompleteScreen
import com.example.readingfoundations.ui.screens.reading_word.WordReadingScreen
import com.example.readingfoundations.ui.screens.quiz.ActiveQuizScreen
import com.example.readingfoundations.ui.screens.quiz.UnitSelectionScreen
import com.example.readingfoundations.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("unit_selection") {
            UnitSelectionScreen(
                onLevelSelected = { levelId ->
                    navController.navigate("active_quiz/$levelId")
                }
            )
        }
        composable(
            "active_quiz/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) {
            ActiveQuizScreen(
                onQuizComplete = {
                    navController.popBackStack("unit_selection", inclusive = false)
                }
            )
        }
        composable("phonetics") { PhoneticsScreen(navController) }
        composable(
            "word_building/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) {
            WordReadingScreen(navController)
        }
        composable(
            "sentence_reading/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) {
            SentenceReadingScreen(navController)
        }
        composable("punctuation") { PunctuationPracticeScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable(
            "level_complete/{level}/{score}/{totalQuestions}",
            arguments = listOf(
                navArgument("level") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 0
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            LevelCompleteScreen(
                navController = navController,
                level = level,
                score = score,
                totalQuestions = totalQuestions
            )
        }
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