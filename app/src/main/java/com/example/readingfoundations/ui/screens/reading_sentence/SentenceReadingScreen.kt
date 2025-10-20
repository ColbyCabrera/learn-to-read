package com.example.readingfoundations.ui.screens.reading_sentence

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
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
                title = { Text("Sentence Building - Level ${uiState.currentLevel}") },
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
                val progress = (quizState.currentQuestionIndex).toFloat() / quizState.questions.size
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "quizProgress"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearWavyProgressIndicator(
                        progress = { animatedProgress },
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
                    PracticeMode(
                        quizState = quizState,
                        onAnswerSelected = { answer -> viewModel.submitAnswer(answer) },
                        onNextClicked = { viewModel.nextQuestion() },
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PracticeMode(
    quizState: SentenceQuizState,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit,
    onSpeakClicked: (String) -> Unit
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val jumbledWords = remember(currentQuestion) { currentQuestion.text.split(" ").shuffled() }

    val selectedIndices = remember(currentQuestion) { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start
    ) {
        Text("Unscramble the sentence:", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                selectedIndices.map { jumbledWords[it] }.forEachIndexed { index, word ->
                    Button(
                        onClick = {
                            selectedIndices.removeAt(index)
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ), shapes = ButtonShapes(
                            shape = ButtonDefaults.shape,
                            pressedShape = ButtonDefaults.mediumPressedShape
                        )
                    ) {
                        Text(word)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Jumbled word bank
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                jumbledWords.forEachIndexed { index, word ->
                    val isSelected = selectedIndices.contains(index)
                    Button(
                        onClick = {
                            if (!isSelected) {
                                selectedIndices.add(index)
                            }
                        }, enabled = !isSelected, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ), shapes = ButtonShapes(
                            shape = ButtonDefaults.shape,
                            pressedShape = ButtonDefaults.mediumPressedShape
                        )
                    ) {
                        Text(word)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {

            ButtonGroup(
                overflowIndicator = { menuState ->
                    { }
                }) {
                val options = listOf("Reset", "Listen")
                val modifiers = listOf(1f, 1.4f)
                val icons = listOf(R.drawable.replay_24px, R.drawable.ear_sound_24px)
                val onClicks: List<() -> Unit> = listOf({
                    selectedIndices.clear()
                }, { onSpeakClicked(currentQuestion.text) })

                options.forEachIndexed { index, option ->
                    customItem(buttonGroupContent = {
                        val interactionSource = remember { MutableInteractionSource() }
                        Button(
                            modifier = Modifier
                                .weight(modifiers[index])
                                .height(ButtonDefaults.LargeContainerHeight)
                                .animateWidth(interactionSource = interactionSource),
                            onClick = onClicks[index],
                            interactionSource = interactionSource,
                            shapes = ButtonShapes(
                                shape = ButtonDefaults.shape,
                                pressedShape = ButtonDefaults.largePressedShape
                            ),
                            enabled = (option != "Reset" || selectedIndices.isNotEmpty()) && quizState.isAnswerCorrect != true,
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Icon(
                                painter = painterResource(icons[index]),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.LargeIconSize)
                            )
                            Spacer(Modifier.width(ButtonDefaults.LargeIconSpacing))
                            Text(
                                text = option,
                                fontSize = 24.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }, menuContent = { })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (quizState.isAnswerCorrect == true) {
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
                        Text(text = "Next Sentence", fontSize = 24.sp)
                    }
                    Text(
                        text = "Correct!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonDefaults.LargeContainerHeight),
                    onClick = {
                        onAnswerSelected(selectedIndices.joinToString(" ") { jumbledWords[it] })
                    },
                    enabled = selectedIndices.size == jumbledWords.size,
                    shapes = ButtonShapes(
                        shape = ButtonDefaults.shape,
                        pressedShape = ButtonDefaults.largePressedShape
                    ),
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.LargeIconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.LargeIconSpacing))
                    Text(text = "Submit", fontSize = 24.sp)
                }
                if (quizState.isAnswerCorrect == false) {
                    Text(
                        text = "Incorrect. Try again!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}