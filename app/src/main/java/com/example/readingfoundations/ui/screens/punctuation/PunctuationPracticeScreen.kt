package com.example.readingfoundations.ui.screens.punctuation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.data.models.PunctuationQuestion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PunctuationPracticeScreen(
    navController: NavController,
    viewModel: PunctuationViewModel = viewModel(factory = com.example.readingfoundations.ui.AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.QuizComplete -> {
                    navController.navigate("levelComplete/4/${event.score}/${uiState.questions.size}") {
                        popUpTo("home")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Punctuation Practice") }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val progress by animateFloatAsState(
                    targetValue = uiState.progress,
                    ProgressIndicatorDefaults.ProgressAnimationSpec
                )
                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    stroke = Stroke(
                        width = WavyProgressIndicatorDefaults.linearIndicatorStroke.width * 2,
                        cap = StrokeCap.Round,
                    ),
                    trackStroke = Stroke(
                        width = WavyProgressIndicatorDefaults.linearTrackStroke.width * 2,
                        cap = StrokeCap.Round
                    ),
                    amplitude = { 0.5F },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuestionContent(
                    question = uiState.questions[uiState.currentQuestionIndex],
                    uiState = uiState,
                    onSubmit = { viewModel.submitAnswer(it) },
                    onNext = { viewModel.nextQuestion() })
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: PunctuationQuestion,
    uiState: PunctuationUiState,
    onSubmit: (String) -> Unit,
    onNext: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = question.text, style = MaterialTheme.typography.headlineSmall
        )

        LazyColumn(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(question.options ?: emptyList()) { option ->
                val isSelected = selectedOption == option
                val cardColors = when {
                    !uiState.isAnswerSubmitted -> CardDefaults.cardColors()
                    isSelected && uiState.isAnswerCorrect -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    isSelected && !uiState.isAnswerCorrect -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    option == question.correctAnswer -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else -> CardDefaults.cardColors()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !uiState.isAnswerSubmitted) {
                            selectedOption = option
                        }, colors = cardColors
                ) {
                    Text(
                        text = option, modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (uiState.isAnswerSubmitted) {
            Text(
                text = if (uiState.isAnswerCorrect) "Correct!" else "Try again!",
                color = if (uiState.isAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onNext, enabled = uiState.isAnswerCorrect
            ) {
                if (uiState.currentQuestionIndex < uiState.questions.size - 1) {
                    Text("Next Question")
                } else {
                    Text("Finish")
                }
            }
        } else {
            Button(
                onClick = { selectedOption?.let { onSubmit(it) } }, enabled = selectedOption != null
            ) {
                Text("Submit")
            }
        }
    }
}