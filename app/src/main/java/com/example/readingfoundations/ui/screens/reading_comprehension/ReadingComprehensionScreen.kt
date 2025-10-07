package com.example.readingfoundations.ui.screens.reading_comprehension

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReadingComprehensionScreen(
    navController: NavController,
    viewModel: ReadingComprehensionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var userAnswer by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReadingComprehensionEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.LevelComplete -> {
                    navController.navigate(
                        "levelComplete/${event.level}/${event.score}/${event.totalQuestions}"
                    )
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(title = {
            Text(
                stringResource(
                    R.string.reading_comprehension_level, uiState.level
                )
            )
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_desc)
                )
            }
        }, actions = {
            if (uiState.level > 0) {
                IconButton(onClick = { viewModel.previousLevel() }) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = stringResource(R.string.previous_level_desc)
                    )
                }
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (uiState.totalQuestions > 0) {
                val progress =
                    (uiState.currentProgress.toFloat() - 1) / uiState.totalQuestions.toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                )
                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
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
            }
            Spacer(modifier = Modifier.height(16.dp))
            uiState.currentText?.let { text ->
                Text(text = text.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.questions.isNotEmpty()) {
                    val question = uiState.questions[uiState.currentQuestionIndex]
                    Text(
                        text = question.question.questionText,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        label = { Text(stringResource(R.string.your_answer)) },
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
                            userAnswer = ""
                        }) {
                            Text(stringResource(R.string.next))
                        }
                    } else {
                        Button(onClick = { viewModel.checkAnswer(userAnswer) }) {
                            Text(stringResource(R.string.check_answer))
                        }
                    }
                } else {
                    Text(stringResource(R.string.loading_questions))
                }
            } ?: Text(stringResource(R.string.loading_text))
        }
    }
}