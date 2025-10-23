package com.example.readingfoundations.ui.screens.phonetics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                })
        }) { paddingValues ->
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
                    onAnswerSelected = {
                        ttsManager.speak(it.ttsText)
                        viewModel.checkAnswer(it)
                    },
                    onNextClicked = { viewModel.nextQuestion() },
                    onSpeakClicked = { ttsManager.speak(it) })

                else -> LearnMode(
                    phonemes = uiState.phonemes,
                    onPhonemeClicked = { ttsManager.speak(it) },
                    onStartPracticeClicked = { viewModel.startPractice() })
            }
        }
    }
}

@Composable
fun LearnMode(
    phonemes: List<Phoneme>, onPhonemeClicked: (String) -> Unit, onStartPracticeClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.phonetics_learn_heading),
            style = MaterialTheme.typography.headlineMedium
        )
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
                    text = phoneme.grapheme, onClick = { onPhonemeClicked(phoneme.ttsText) })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartPracticeClicked,
            enabled = phonemes.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(stringResource(R.string.start_practice))
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PracticeMode(
    quizState: QuizState,
    onAnswerSelected: (Phoneme) -> Unit,
    onNextClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val questionPromptText = when (quizState.questionPrompt) {
        is UiText.StringResource -> stringResource(
            quizState.questionPrompt.resId, *quizState.questionPrompt.args
        )

        else -> ""
    }
    val progress = (quizState.currentQuestionIndex).toFloat() / quizState.questions.size
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "quizProgress"
    )

    LaunchedEffect(quizState.currentQuestionIndex) {
        onSpeakClicked(questionPromptText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            stroke = Stroke(
                WavyProgressIndicatorDefaults.linearIndicatorStroke.width * 2, cap = StrokeCap.Round
            ),
            trackStroke = Stroke(
                WavyProgressIndicatorDefaults.linearTrackStroke.width * 2, cap = StrokeCap.Round
            ),
            amplitude = { 0.5F },
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
        Text(
            text = questionPromptText,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { onSpeakClicked(questionPromptText) })

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(quizState.options, key = { it.id }) { option ->
                val isTarget = option.id == quizState.targetPhoneme?.id
                val isSelected = option.id == quizState.selectedOption?.id

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        quizState.isAnswerCorrect != null && isTarget -> MaterialTheme.colorScheme.primaryContainer
                        quizState.isAnswerCorrect == false && isSelected -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }, animationSpec = tween(durationMillis = 500)
                )

                val textToShow = when (quizState.questionType) {
                    QuestionType.GRAPHEME_TO_WORD -> option.exampleWord
                    QuestionType.GRAPHEME_TO_SOUND -> option.sound
                    else -> option.grapheme
                }

                PhonemeCard(
                    text = textToShow, color = backgroundColor, onClick = {
                        if (quizState.isAnswerCorrect == null) { // Prevent clicking after an answer
                            onAnswerSelected(option)
                        }
                    })
            }
        }
        if (quizState.isAnswerCorrect != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonDefaults.LargeContainerHeight),
                    onClick = onNextClicked,
                    shapes = ButtonShapes(
                        shape = ButtonDefaults.shape,
                        pressedShape = ButtonDefaults.largePressedShape
                    ),
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward_24px),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.LargeIconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.LargeIconSpacing))
                    Text(text = stringResource(R.string.next_phoneme), fontSize = 24.sp)
                }

                val (feedbackText, feedbackColor) = if (quizState.isAnswerCorrect) {
                    stringResource(R.string.correct_feedback) to MaterialTheme.colorScheme.primary
                } else {
                    stringResource(R.string.incorrect_feedback_highlighted) to MaterialTheme.colorScheme.error
                }

                Text(
                    text = feedbackText,
                    color = feedbackColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            // Add a listen button when no answer has been submitted
            OutlinedButton(
                onClick = { onSpeakClicked(questionPromptText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonDefaults.LargeContainerHeight)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ear_sound_24px),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.LargeIconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.LargeIconSpacing))
                Text(text = stringResource(R.string.listen), fontSize = 24.sp)
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
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
