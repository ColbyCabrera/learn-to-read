package com.example.readingfoundations.ui.screens.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.data.models.allUnits

@Composable
fun UnitSelectionScreen(
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            // Can add a TopAppBar here if needed
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier.padding(innerPadding)
        ) {
            items(allUnits) { unit ->
                UnitItem(
                    unit = unit,
                    onLevelSelected = onLevelSelected,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun UnitItem(
    unit: Unit,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = unit.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            unit.levels.forEach { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLevelSelected(level.level) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Level ${level.level}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}