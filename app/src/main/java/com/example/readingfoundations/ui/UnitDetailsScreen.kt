package com.example.readingfoundations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingfoundations.R
import com.example.readingfoundations.R
import com.example.readingfoundations.data.ContentType
import com.example.readingfoundations.data.Level

@Composable
fun UnitDetailsScreen(
    onLevelClick: (ContentType, Int) -> Unit,
    viewModel: UnitDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UnitDetailsUiState.Success -> {
            UnitDetailsContent(unit = state.unit, onLevelClick = onLevelClick)
        }
        UnitDetailsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        UnitDetailsUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.unit_not_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun UnitDetailsContent(
    unit: com.example.readingfoundations.data.Unit,
    onLevelClick: (ContentType, Int) -> Unit
) {
    LazyColumn {
        unit.levels.forEach { (contentType, levels) ->
            item {
                Text(
                    text = contentType.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(levels) { level ->
                LevelCard(
                    level = level,
                    onLevelClick = { onLevelClick(contentType, level.difficulty) }
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: Level,
    onLevelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onLevelClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Level ${level.value}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}