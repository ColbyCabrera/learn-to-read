package com.example.readingfoundations.ui.screens.reading_sentence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.data.models.Sentence
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SentenceReadingScreen(
    navController: NavController,
    viewModel: SentenceReadingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.LevelComplete -> {
                            navController.navigate("level_complete/${event.level}/${event.score}/${event.totalQuestions}") {
                                popUpTo("home")
                            }
                        }
                    }
                }
            }
        }
    }

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
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.sentences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator()
                }
            } else if (uiState.isPracticeMode && uiState.quizState != null) {
                val quizState = uiState.quizState!!
                val progress =
                    (quizState.currentQuestionIndex + 1).toFloat() / quizState.questions.size
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearWavyProgressIndicator(
                        progress = { progress }, modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Text(
                        text = "Sentence ${quizState.currentQuestionIndex + 1} of ${quizState.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PracticeMode(
                        quizState = quizState,
                        onAnswerSubmitted = { answer -> viewModel.submitAnswer(answer) },
                        onNextClicked = { viewModel.nextQuestion() },
                        onTryAgainClicked = viewModel::tryAgain,
                        onSpeakClicked = { text -> ttsManager.speak(text) })
                }
            } else {
                LearnMode(
                    sentences = uiState.sentences,
                    onSentenceClicked = { sentence -> ttsManager.speak(sentence) },
                    onStartPracticeClicked = { viewModel.startPractice() })
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
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Learn these sentences:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)
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
        Button(
            onClick = onStartPracticeClicked,
            enabled = sentences.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Start Practice")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeMode(
    quizState: SentenceQuizState,
    onAnswerSubmitted: (String) -> Unit,
    onNextClicked: () -> Unit,
    onTryAgainClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val jumbledWords = remember(currentQuestion) { currentQuestion.text.split(" ").shuffled() }
    val assembledWords = remember(currentQuestion) { mutableStateListOf<String>() }
    val remainingWords =
        remember(currentQuestion) { mutableStateListOf(*jumbledWords.toTypedArray()) }


    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unscramble the sentence:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onSpeakClicked(currentQuestion.text) }) {
            Text("Hear the sentence")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Assembled sentence display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 100.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                assembledWords.forEachIndexed { index, word ->
                    Button(
                        onClick = {
                            remainingWords.add(word)
                            assembledWords.removeAt(index)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(word)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Jumbled words bank
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            remainingWords.forEachIndexed { index, word ->
                Button(onClick = {
                    assembledWords.add(word)
                    remainingWords.removeAt(index)
                }) {
                    Text(word)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onAnswerSubmitted(assembledWords.joinToString(" ")) },
                enabled = quizState.isAnswerCorrect == null && remainingWords.isEmpty()
            ) {
                Text("Submit")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                assembledWords.clear()
                remainingWords.clear()
                remainingWords.addAll(jumbledWords)
            }) {
                Text("Reset")
            }
        }

        quizState.isAnswerCorrect?.let { isCorrect ->
            Text(
                text = if (isCorrect) "Correct!" else "Incorrect!",
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isCorrect) {
                    Button(onClick = onNextClicked) {
                        Text("Next Sentence")
                    }
                } else {
                    Button(onClick = {
                        onTryAgainClicked()
                        assembledWords.clear()
                        remainingWords.clear()
                        remainingWords.addAll(jumbledWords)
                    }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}
