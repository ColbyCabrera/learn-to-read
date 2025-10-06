package com.example.readingfoundations.ui.screens.reading_comprehension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingComprehensionScreen(
    navController: NavController,
    viewModel: ReadingComprehensionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var userAnswer by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState.feedback) {
        if (uiState.feedback.isNotEmpty() && (uiState.feedback.contains("challenging") || uiState.feedback.contains("easier"))) {
            // Show a snackbar or dialog with the feedback message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Comprehension - Level ${uiState.level}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                    }
                },
                actions = {
                    if (uiState.level > 0) {
                        IconButton(onClick = { viewModel.previousLevel() }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Go to previous level")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            uiState.currentText?.let { text ->
                Text(text = text.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.questions.isNotEmpty()) {
                    val question = uiState.questions[uiState.currentQuestionIndex]
                    Text(text = question.questionText, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        label = { Text("Your answer") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.answerChecked
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.feedback.isNotEmpty()) {
                        Text(
                            text = uiState.feedback,
                            color = if (uiState.isCorrect == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState.answerChecked) {
                        Button(onClick = {
                            viewModel.nextQuestion()
                            userAnswer = TextFieldValue("")
                        }) {
                            Text("Next")
                        }
                    } else {
                        Button(onClick = { viewModel.checkAnswer(userAnswer.text) }) {
                            Text("Check Answer")
                        }
                    }
                } else {
                    Text("Loading questions...")
                }
            } ?: Text("Loading text...")
        }
    }
}