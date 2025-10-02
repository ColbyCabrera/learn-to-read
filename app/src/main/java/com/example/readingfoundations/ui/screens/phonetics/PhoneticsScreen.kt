package com.example.readingfoundations.ui.screens.phonetics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
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

    LaunchedEffect(uiState.targetPhoneme) {
        uiState.targetPhoneme?.let {
            ttsManager.speak(it.ttsText)
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
        if (uiState.inPracticeMode) {
            PracticeContent(
                uiState = uiState,
                onOptionSelected = { viewModel.checkAnswer(it) },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            AllPhonemesContent(
                viewModel = viewModel,
                ttsManager = ttsManager,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun AllPhonemesContent(
    viewModel: PhoneticsViewModel,
    ttsManager: TextToSpeechManager,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(viewModel.phonemes) { phoneme ->
            PhonemeCard(
                phoneme = phoneme,
                onClick = {
                    viewModel.onLetterSelected(phoneme)
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.listen_and_choose),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(uiState.options) { option ->
                val color = when (uiState.isCorrect) {
                    true -> if (option == uiState.targetPhoneme) Color.Green else Color.Unspecified
                    false -> if (option == uiState.selectedPhoneme) Color.Red else Color.Unspecified
                    null -> Color.Unspecified
                }
                PracticePhonemeCard(
                    phoneme = option,
                    color = color,
                    onClick = {
                        if (uiState.isCorrect == null) { // prevent clicking after an answer
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
    phoneme: Phoneme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = phoneme.sound,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = phoneme.exampleWord,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun PracticePhonemeCard(
    phoneme: Phoneme,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(color).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Text(
                text = phoneme.sound,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = phoneme.exampleWord,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}