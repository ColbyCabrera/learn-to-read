package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("phonetics") }, modifier = Modifier.padding(8.dp)) {
            Text("Phonetics")
        }
        Button(onClick = { navController.navigate("word_building") }, modifier = Modifier.padding(8.dp)) {
            Text("Word Building")
        }
        Button(onClick = { navController.navigate("sentence_reading") }, modifier = Modifier.padding(8.dp)) {
            Text("Sentence Reading")
        }
        Button(onClick = { navController.navigate("punctuation") }, modifier = Modifier.padding(8.dp)) {
            Text("Punctuation")
        }
        Button(onClick = { navController.navigate("settings") }, modifier = Modifier.padding(8.dp)) {
            Text("Settings")
        }
    }
}