package com.example.readingfoundations.ui.screens.phonetics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.data.models.Phoneme
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneticsScreen(
    navController: NavController,
    viewModel: PhoneticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
            viewModel.stopPractice()
        }
    }

    LaunchedEffect(uiState.questionPrompt) {
        uiState.questionPrompt?.let {
            ttsManager.speak(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.phonetics_practice)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (uiState.inPracticeMode) {
                            viewModel.stopPractice()
                        } else {
                            viewModel.startPractice()
                        }
                    }) {
                        Icon(
                            imageVector = if (uiState.inPracticeMode) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.inPracticeMode) stringResource(R.string.stop_practice_desc) else stringResource(R.string.start_practice_desc)
                        )
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
                uiState.inPracticeMode -> PracticeContent(
                    uiState = uiState,
                    onOptionSelected = { viewModel.checkAnswer(it) }
                )
                else -> AllPhonemesContent(
                    phonemes = uiState.allPhonemes,
                    ttsManager = ttsManager
                )
            }
        }
    }
}

@Composable
fun AllPhonemesContent(
    phonemes: List<Phoneme>,
    ttsManager: TextToSpeechManager,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(phonemes, key = { it.id }) { phoneme ->
            PhonemeCard(
                text = phoneme.grapheme,
                onClick = {
                    ttsManager.speak(phoneme.ttsText)
                }
            )
        }
    }
}

@Composable
fun PracticeContent(
    uiState: PhoneticsUiState,
    onOptionSelected: (Phoneme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = uiState.questionPrompt ?: "",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(300.dp) // A fixed height to prevent scrolling issues
        ) {
            items(uiState.options, key = { it.id }) { option ->
                val isTarget = option.id == uiState.targetPhoneme?.id
                val isSelected = option.id == uiState.selectedOption?.id

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        uiState.isCorrect == true && isTarget -> MaterialTheme.colorScheme.primaryContainer
                        uiState.isCorrect == false && isSelected -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(durationMillis = 500)
                )

                val textToShow = when (uiState.questionType) {
                    QuestionType.GRAPHEME_TO_WORD -> option.exampleWord
                    QuestionType.GRAPME_TO_SOUND -> option.sound
                    else -> option.grapheme
                }

                PhonemeCard(
                    text = textToShow,
                    color = backgroundColor,
                    onClick = {
                        if (uiState.isCorrect == null) { // Prevent clicking after an answer
                            onOptionSelected(option)
                        }
                    }
                )
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