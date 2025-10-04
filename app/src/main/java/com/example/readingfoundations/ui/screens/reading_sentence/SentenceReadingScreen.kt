package com.example.readingfoundations.ui.screens.reading_sentence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceReadingScreen(
    navController: NavController,
    viewModel: SentenceReadingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentence Reading - Level ${uiState.currentLevel}") },
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
            if (uiState.isPracticeMode && uiState.quizState != null) {
                PracticeMode(
                    quizState = uiState.quizState!!,
                    onAnswerSubmitted = { answer -> viewModel.submitAnswer(answer) },
                    onNextClicked = { viewModel.nextQuestion() },
                    onSpeakClicked = { text -> ttsManager.speak(text) }
                )
            } else {
                LearnMode(
                    sentences = uiState.sentences,
                    onSentenceClicked = { sentence -> ttsManager.speak(sentence) },
                    onStartPracticeClicked = { viewModel.startPractice() }
                )
            }
        }
    }
}

@Composable
fun LearnMode(
    sentences: List<Sentence>,
    onSentenceClicked: (String) -> Unit,
    onStartPracticeClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Learn these sentences:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sentences) { sentence ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSentenceClicked(sentence.text) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = sentence.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartPracticeClicked, enabled = sentences.isNotEmpty()) {
            Text("Start Practice")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PracticeMode(
    quizState: SentenceQuizState,
    onAnswerSubmitted: (String) -> Unit,
    onNextClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val jumbledWords = remember(currentQuestion) { currentQuestion.text.split(" ").shuffled() }
    val assembledWords = remember(currentQuestion) { mutableStateListOf<String>() }
    val remainingWords = remember(currentQuestion) { mutableStateListOf(*jumbledWords.toTypedArray()) }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Unscramble the sentence:",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { onSpeakClicked(currentQuestion.text) }) {
                Text("Hear the sentence")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Assembled sentence display
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp),
            ) {
                if (assembledWords.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Tap words below to build the sentence",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assembledWords.forEach { word ->
                            Text(
                                text = word,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Jumbled words bank
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                remainingWords.forEachIndexed { index, word ->
                    FilledTonalButton(onClick = {
                        assembledWords.add(word)
                        remainingWords.removeAt(index)
                    }) {
                        Text(word)
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            quizState.isAnswerCorrect?.let { isCorrect ->
                Text(
                    text = if (isCorrect) "Correct!" else "Incorrect. The answer is: ${currentQuestion.text}",
                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Button(onClick = onNextClicked, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Sentence")
                }
            }

            if (quizState.isAnswerCorrect == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onAnswerSubmitted(assembledWords.joinToString(" ")) },
                        enabled = remainingWords.isEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                    OutlinedButton(onClick = {
                        assembledWords.clear()
                        remainingWords.clear()
                        remainingWords.addAll(jumbledWords)
                    }) {
                        Text("Reset")
                    }
                    IconButton(
                        onClick = {
                            if (assembledWords.isNotEmpty()) {
                                val lastWord = assembledWords.removeLast()
                                remainingWords.add(lastWord)
                            }
                        },
                        enabled = assembledWords.isNotEmpty()
                    ) {
                        Icon(Icons.Outlined.Backspace, contentDescription = "Backspace")
                    }
                }
            }
        }
    }
}