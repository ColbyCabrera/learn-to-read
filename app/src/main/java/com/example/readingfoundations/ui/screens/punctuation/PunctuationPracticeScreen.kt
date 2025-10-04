package com.example.readingfoundations.ui.screens.punctuation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.ui.AppViewModelProvider
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunctuationPracticeScreen(
    navController: NavController,
    viewModel: PunctuationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.questions.isEmpty()) {
                CircularProgressIndicator()
            } else {
                AnimatedContent(
                    targetState = uiState.currentQuestionIndex,
                    label = "question"
                ) {
                    PunctuationQuestionCard(
                        uiState = uiState,
                        onAnswerSelected = { answer -> viewModel.submitAnswer(answer) },
                        onNextClicked = { viewModel.nextQuestion() }
                    )
                }
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
    var userAnswer by remember(question) { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (question.options.isNullOrEmpty()) {
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    label = { Text("Your answer") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = uiState.isAnswerSubmitted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onAnswerSelected(userAnswer) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    enabled = !uiState.isAnswerSubmitted && userAnswer.isNotBlank()
                ) {
                    Text(text = "Submit")
                }
            } else {
                question.options.forEach { option ->
                    FilledTonalButton(
                        onClick = { onAnswerSelected(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        enabled = !uiState.isAnswerSubmitted,
                    ) {
                        Text(text = option)
                    }
                }
            }

            if (uiState.isAnswerSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (uiState.isAnswerCorrect) "Correct!" else "Incorrect! The answer is: ${question.correctAnswer}",
                    color = if (uiState.isAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNextClicked, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.currentQuestionIndex < uiState.questions.size - 1) "Next Question" else "Finish Quiz")
                }
            }
        }
    }
}