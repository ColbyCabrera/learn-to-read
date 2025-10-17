package com.example.readingfoundations.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingfoundations.ui.screens.home.HomeScreen
import com.example.readingfoundations.ui.screens.phonetics.PhoneticsScreen
import com.example.readingfoundations.ui.screens.reading_comprehension.ReadingComprehensionScreen
import com.example.readingfoundations.ui.screens.reading_sentence.SentenceReadingScreen
import com.example.readingfoundations.ui.screens.reading_word.LevelCompleteScreen
import com.example.readingfoundations.ui.screens.reading_word.WordReadingScreen
import com.example.readingfoundations.ui.screens.subjects.SubjectsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("subjects") {
            SubjectsScreen(navController = navController)
        }
        composable("phonetics") {
            PhoneticsScreen(navController = navController)
        }
        composable(
            route = "reading_word/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) {
            WordReadingScreen(navController = navController)
        }
        composable(
            route = "levelComplete/{level}/{score}/{totalQuestions}",
            arguments = listOf(
                navArgument("level") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
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
            "sentence_reading/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) {
            SentenceReadingScreen(
                navController = navController
            )
        }
        composable("reading_comprehension") {
            ReadingComprehensionScreen(navController = navController)
        }
    }
}