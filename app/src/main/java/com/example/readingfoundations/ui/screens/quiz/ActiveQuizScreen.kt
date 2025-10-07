package com.example.readingfoundations.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingfoundations.ui.AppViewModelProvider

@Composable
fun ActiveQuizScreen(
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onQuizComplete: () -> Unit
) {
    val quizUiState by quizViewModel.uiState.collectAsState()

    if (quizUiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (quizUiState.isQuizOver) {
        QuizCompletionContent(
            score = quizUiState.score,
            totalQuestions = quizUiState.questions.size,
            onQuizComplete = onQuizComplete
        )
    } else {
        QuizContent(
            quizUiState = quizUiState,
            onAnswerSelected = quizViewModel::selectAnswer,
            onCheckAnswer = quizViewModel::checkAnswer,
            onNextQuestion = quizViewModel::nextQuestion
        )
    }
}

@Composable
fun QuizContent(
    quizUiState: QuizUiState,
    onAnswerSelected: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = quizUiState.currentQuestion ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Question ${quizUiState.currentQuestionIndex + 1} of ${quizUiState.questions.size}",
            style = MaterialTheme.typography.labelLarge
        )
        LinearProgressIndicator(
            progress = { (quizUiState.currentQuestionIndex + 1).toFloat() / quizUiState.questions.size },
            modifier = Modifier.fillMaxWidth()
        )

        when (question) {
            is QuizQuestion.PhonemeQuestion -> PhonemeQuestionContent(question, quizUiState.userAnswer, onAnswerSelected)
            is QuizQuestion.WordQuestion -> WordQuestionContent(question, quizUiState.userAnswer, onAnswerSelected)
            is QuizQuestion.SentenceQuestion -> SentenceQuestionContent(question, quizUiState.userAnswer, onAnswerSelected)
            is QuizQuestion.PunctuationQuestionItem -> PunctuationQuestionContent(question, quizUiState.userAnswer, onAnswerSelected)
        }

        if (quizUiState.isAnswerCorrect != null) {
            val feedback = if (quizUiState.isAnswerCorrect) "Correct!" else "Try Again"
            Text(text = feedback)
            Button(onClick = onNextQuestion) {
                Text("Next")
            }
        } else {
            Button(onClick = onCheckAnswer, enabled = quizUiState.userAnswer.isNotBlank()) {
                Text("Check Answer")
            }
        }
    }
}

@Composable
fun PhonemeQuestionContent(question: QuizQuestion.PhonemeQuestion, userAnswer: String, onAnswerSelected: (String) -> Unit) {
    Text(text = "What letters make the sound in \"${question.phoneme.exampleWord}\"?")
    OutlinedTextField(
        value = userAnswer,
        onValueChange = onAnswerSelected,
        label = { Text("Your Answer") }
    )
}

@Composable
fun WordQuestionContent(question: QuizQuestion.WordQuestion, userAnswer: String, onAnswerSelected: (String) -> Unit) {
    Text(text = "Spell the word you hear.")
    Text(text = "(Pretend you heard: ${question.word.text})", style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(
        value = userAnswer,
        onValueChange = onAnswerSelected,
        label = { Text("Your Answer") }
    )
}

@Composable
fun SentenceQuestionContent(question: QuizQuestion.SentenceQuestion, userAnswer: String, onAnswerSelected: (String) -> Unit) {
    Text(text = "Write the sentence you hear.")
    Text(text = "(Pretend you heard: ${question.sentence.text})", style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(
        value = userAnswer,
        onValueChange = onAnswerSelected,
        label = { Text("Your Answer") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PunctuationQuestionContent(question: QuizQuestion.PunctuationQuestionItem, userAnswer: String, onAnswerSelected: (String) -> Unit) {
    val punctuationQuestion = question.punctuationQuestion
    Text(text = punctuationQuestion.text)

    if (punctuationQuestion.options != null) {
        // Multiple choice
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            punctuationQuestion.options.forEach { option ->
                Button(
                    onClick = { onAnswerSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (option == userAnswer) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(option)
                }
            }
        }
    } else {
        // Fill-in-the-blank
        OutlinedTextField(
            value = userAnswer,
            onValueChange = onAnswerSelected,
            label = { Text("Your Answer") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuizCompletionContent(
    score: Int,
    totalQuestions: Int,
    onQuizComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Your score: $score / $totalQuestions",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(onClick = onQuizComplete) {
            Text("Back to Home")
        }
    }
}