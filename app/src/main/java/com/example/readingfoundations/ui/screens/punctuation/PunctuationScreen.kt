package com.example.readingfoundations.ui.screens.punctuation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunctuationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punctuation") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PunctuationCard(
                symbol = ".",
                name = "Period",
                description = "A period is used to end a sentence that makes a statement.",
                example = "The dog is brown."
            )
            Spacer(modifier = Modifier.height(16.dp))
            PunctuationCard(
                symbol = "?",
                name = "Question Mark",
                description = "A question mark is used at the end of a sentence that asks a question.",
                example = "What is your name?"
            )
            Spacer(modifier = Modifier.height(16.dp))
            PunctuationCard(
                symbol = ",",
                name = "Comma",
                description = "A comma is used to separate items in a list or to pause between parts of a sentence.",
                example = "I like apples, bananas, and oranges."
            )
        }
    }
}

@Composable
fun PunctuationCard(symbol: String, name: String, description: String, example: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$symbol $name",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Example: \"$example\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Italic
            )
        }
    }
}