package com.example.readingfoundations.ui.screens.reading_word

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.readingfoundations.data.models.Word
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.utils.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
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

    val selectedIndices = remember(currentQuestion) { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start
    ) {
        Text("Unscramble the word:", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.size(IconButtonDefaults.smallContainerSize()))
                Text(
                    text = selectedIndices.map { jumbledLetters[it] }.joinToString(" "),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = quizState.isAnswerCorrect != true,
                    onClick = {
                        if (selectedIndices.isNotEmpty()) {
                            selectedIndices.removeLast()
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.backspace_24px),
                        contentDescription = "Backspace"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Jumbled letter bank
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
                jumbledLetters.forEachIndexed { index, char ->
                    val isSelected = selectedIndices.contains(index)
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val cornerRadius by animateDpAsState(
                        targetValue = if (isPressed) 0.dp else 32.dp,
                        label = "shapeMorph"
                    )
                    Button(
                        modifier = Modifier.size(64.dp),
                        contentPadding = PaddingValues(
                            horizontal = 0.dp,
                            vertical = ButtonDefaults.ContentPadding.calculateTopPadding()
                        ),
                        onClick = {
                            if (!isSelected) {
                                selectedIndices.add(index)
                            }
                        },
                        enabled = !isSelected,
                        shape = RoundedCornerShape(cornerRadius),
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(char.toString(), style = MaterialTheme.typography.headlineMedium)
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
                    customItem(
                        buttonGroupContent = {
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
                                enabled = option != "Reset" || selectedIndices.isNotEmpty(),
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
                        },
                        menuContent = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonDefaults.LargeContainerHeight),
                onClick = {
                    onAnswerSelected(selectedIndices.map { jumbledLetters[it] }.joinToString(""))
                },
                enabled = quizState.isAnswerCorrect != true && selectedIndices.size == jumbledLetters.size,
                shapes = ButtonShapes(
                    shape = ButtonDefaults.shape,
                    pressedShape = ButtonDefaults.largePressedShape
                ),
                colors = ButtonDefaults.buttonColors(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary
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
        }

        quizState.isAnswerCorrect?.let { isCorrect ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
}