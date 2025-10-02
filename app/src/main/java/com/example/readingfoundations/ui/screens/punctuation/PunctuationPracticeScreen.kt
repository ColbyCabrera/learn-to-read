package com.example.readingfoundations.ui.screens.punctuation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.data.models.PunctuationQuestion
import com.example.readingfoundations.ui.AppViewModelProvider
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunctuationPracticeScreen(
    navController: NavController,
    viewModel: PunctuationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.navigationEvent, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is NavigationEvent.QuizComplete -> {
                        navController.navigate("quiz_complete/${event.score}/${event.totalQuestions}")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punctuation Practice") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                PunctuationQuestionCard(
                    uiState = uiState,
                    onAnswerSelected = { answer -> viewModel.submitAnswer(answer) },
                    onNextClicked = { viewModel.nextQuestion() }
                )
            }
        }
    }
}

@Composable
fun PunctuationQuestionCard(
    uiState: PunctuationUiState,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    val question = uiState.questions.getOrNull(uiState.currentQuestionIndex) ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        question.options.forEach { option ->
            val isSelected = uiState.isAnswerSubmitted && option == question.correctAnswer
            val isIncorrect = uiState.isAnswerSubmitted && !uiState.isAnswerCorrect && option != question.correctAnswer

            Button(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                enabled = !uiState.isAnswerSubmitted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        isIncorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(text = option)
            }
        }

        if (uiState.isAnswerSubmitted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (uiState.isAnswerCorrect) "Correct!" else "Incorrect!",
                color = if (uiState.isAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNextClicked) {
                Text(if (uiState.currentQuestionIndex < uiState.questions.size - 1) "Next" else "Finish")
            }
        }
    }
}