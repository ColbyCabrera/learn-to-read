package com.example.readingfoundations.ui.screens.reading_word

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
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.data.models.Word
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordReadingScreen(
    navController: NavController,
    viewModel: WordReadingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

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
                title = { Text("Word Building - Level ${uiState.currentLevel}") },
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
            if (uiState.words.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator()
                }
            } else if (uiState.isPracticeMode && uiState.quizState != null) {
                val quizState = uiState.quizState!!
                val progress =
                    (quizState.currentQuestionIndex + 1).toFloat() / quizState.questions.size
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        stroke = Stroke(
                            WavyProgressIndicatorDefaults.linearIndicatorStroke.width * 2,
                            cap = StrokeCap.Round
                        ),
                        trackStroke = Stroke(
                            WavyProgressIndicatorDefaults.linearTrackStroke.width * 2,
                            cap = StrokeCap.Round
                        ),
                    )
                    Text(
                        text = "${quizState.currentQuestionIndex + 1} of ${quizState.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.End),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PracticeMode(
                        quizState = quizState,
                        onAnswerSelected = { answer -> viewModel.submitAnswer(answer) },
                        onNextClicked = { viewModel.nextQuestion() },
                        onSpeakClicked = { text -> ttsManager.speak(text) })
                }
            } else {
                LearnMode(
                    words = uiState.words,
                    onWordClicked = { word -> ttsManager.speak(word) },
                    onStartPracticeClicked = { viewModel.startPractice() })
            }
        }
    }
}

@Composable
fun LearnMode(
    words: List<Word>, onWordClicked: (String) -> Unit, onStartPracticeClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Learn these words:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)
        ) {
            items(words) { word ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWordClicked(word.text) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = word.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartPracticeClicked,
            enabled = words.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Start Practice")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeMode(
    quizState: QuizState,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val jumbledLetters =
        remember(currentQuestion) { currentQuestion.text.toCharArray().toList().shuffled() }

    val assembledAnswer = remember(currentQuestion) { mutableStateListOf<Char>() }
    val remainingLetters =
        remember(currentQuestion) { mutableStateListOf(*jumbledLetters.toTypedArray()) }

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unscramble the word:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onSpeakClicked(currentQuestion.text) }) {
            Text("Hear the word")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Assembled answer display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = assembledAnswer.joinToString(" "),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Jumbled letter bank
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            remainingLetters.forEachIndexed { index, char ->
                Button(onClick = {
                    assembledAnswer.add(char)
                    remainingLetters.removeAt(index)
                }) {
                    Text(char.toString(), style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onAnswerSelected(assembledAnswer.joinToString("")) },
                enabled = quizState.isAnswerCorrect == null && remainingLetters.isEmpty()
            ) {
                Text("Submit")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                assembledAnswer.clear()
                remainingLetters.clear()
                remainingLetters.addAll(jumbledLetters)
            }) {
                Text("Reset")
            }
        }

        quizState.isAnswerCorrect?.let { isCorrect ->
            Text(
                text = if (isCorrect) "Correct!" else "Incorrect. Try again!",
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (isCorrect) {
                Button(onClick = onNextClicked, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Next Word")
                }
            }
        }
    }
}
