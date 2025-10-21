package com.example.readingfoundations.ui.screens.phonetics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.data.models.Phoneme
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneticsScreen(
    navController: NavController,
    viewModel: PhoneticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                            navController.navigate("levelComplete/${event.level}/${event.score}/${event.totalQuestions}") {
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
                title = { Text(stringResource(R.string.phonetics_practice) + " - Level ${uiState.currentLevel}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.isPracticeMode && uiState.quizState != null -> PracticeMode(
                    quizState = uiState.quizState!!,
                    onAnswerSelected = { viewModel.checkAnswer(it) },
                    onNextClicked = { viewModel.nextQuestion() }
                )
                else -> LearnMode(
                    phonemes = uiState.phonemes,
                    onPhonemeClicked = { ttsManager.speak(it) },
                    onStartPracticeClicked = { viewModel.startPractice() }
                )
            }
        }
    }
}

@Composable
fun LearnMode(
    phonemes: List<Phoneme>,
    onPhonemeClicked: (String) -> Unit,
    onStartPracticeClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Learn these phonemes:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(phonemes, key = { it.id }) { phoneme ->
                PhonemeCard(
                    text = phoneme.grapheme,
                    onClick = { onPhonemeClicked(phoneme.ttsText) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartPracticeClicked,
            enabled = phonemes.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Start Practice")
        }
    }
}


@Composable
fun PracticeMode(
    quizState: QuizState,
    onAnswerSelected: (Phoneme) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = quizState.questionPrompt ?: "",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(300.dp)
        ) {
            items(quizState.options, key = { it.id }) { option ->
                val isTarget = option.id == quizState.targetPhoneme?.id
                val isSelected = option.id == quizState.selectedOption?.id

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        quizState.isAnswerCorrect == true && isTarget -> MaterialTheme.colorScheme.primaryContainer
                        quizState.isAnswerCorrect == false && isSelected -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(durationMillis = 500)
                )

                val textToShow = when (quizState.questionType) {
                    QuestionType.GRAPHEME_TO_WORD -> option.exampleWord
                    QuestionType.GRAPHEME_TO_SOUND -> option.sound
                    else -> option.grapheme
                }

                PhonemeCard(
                    text = textToShow,
                    color = backgroundColor,
                    onClick = {
                        if (quizState.isAnswerCorrect == null) { // Prevent clicking after an answer
                            onAnswerSelected(option)
                        }
                    }
                )
            }
        }
        if (quizState.isAnswerCorrect == true) {
            Button(onClick = onNextClicked) {
                Text("Next")
            }
        }
    }
}

@Composable
fun PhonemeCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
