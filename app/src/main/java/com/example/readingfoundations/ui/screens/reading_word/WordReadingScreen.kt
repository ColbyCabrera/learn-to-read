package com.example.readingfoundations.ui.screens.reading_word

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.readingfoundations.data.models.Word
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordReadingScreen(
    navController: NavController,
    viewModel: WordReadingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.navigationEvent, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.LevelComplete -> {
                        navController.navigate("level_complete/${event.level}")
                    }
                }
            }
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        viewModel.loadWords()
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
            if (uiState.words.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.isPracticeMode && uiState.quizState != null) {
                PracticeMode(
                    quizState = uiState.quizState!!,
                    onAnswerSelected = { answer -> viewModel.submitAnswer(answer) },
                    onNextClicked = { viewModel.nextQuestion() },
                    onSpeakClicked = { text -> ttsManager.speak(text) }
                )
            } else {
                LearnMode(
                    words = uiState.words,
                    onWordClicked = { word -> ttsManager.speak(word) },
                    onStartPracticeClicked = { viewModel.startPractice() }
                )
            }
        }
    }
}

@Composable
fun LearnMode(
    words: List<Word>,
    onWordClicked: (String) -> Unit,
    onStartPracticeClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Learn these words:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
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
        Button(onClick = onStartPracticeClicked, enabled = words.isNotEmpty()) {
            Text("Start Practice")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PracticeMode(
    quizState: QuizState,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val jumbledLetters = remember(currentQuestion) { currentQuestion.text.toCharArray().toList().shuffled() }

    val assembledAnswer = remember(currentQuestion) { mutableStateListOf<Char>() }
    val remainingLetters = remember(currentQuestion) { mutableStateListOf(*jumbledLetters.toTypedArray()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Unscramble the word:",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { onSpeakClicked(currentQuestion.text) }) {
                Text("Hear the word")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Assembled answer display
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 80.dp),
            ) {
                if (assembledAnswer.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Tap letters below",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assembledAnswer.forEach { char ->
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Jumbled letter bank
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                remainingLetters.forEachIndexed { index, char ->
                    FilledTonalButton(
                        onClick = {
                            assembledAnswer.add(char)
                            remainingLetters.removeAt(index)
                        },
                        modifier = Modifier.sizeIn(minWidth = 52.dp, minHeight = 52.dp)
                    ) {
                        Text(char.toString(), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            quizState.isAnswerCorrect?.let { isCorrect ->
                Text(
                    text = if (isCorrect) "Correct!" else "Incorrect. The answer is ${currentQuestion.text}",
                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isCorrect || !isCorrect) { // Show next button always after submission
                    Button(onClick = onNextClicked, modifier = Modifier.fillMaxWidth()) {
                        Text("Next Word")
                    }
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
                        onClick = { onAnswerSelected(assembledAnswer.joinToString("")) },
                        enabled = remainingLetters.isEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                    OutlinedButton(onClick = {
                        assembledAnswer.clear()
                        remainingLetters.clear()
                        remainingLetters.addAll(jumbledLetters)
                    }) {
                        Text("Reset")
                    }
                    IconButton(
                        onClick = {
                            if (assembledAnswer.isNotEmpty()) {
                                val lastChar = assembledAnswer.removeLast()
                                remainingLetters.add(lastChar)
                            }
                        },
                        enabled = assembledAnswer.isNotEmpty()
                    ) {
                        Icon(Icons.Outlined.Backspace, contentDescription = "Backspace")
                    }
                }
            }
        }
    }
}