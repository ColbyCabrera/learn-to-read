package com.example.readingfoundations.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.ui.AppViewModelProvider

private data class MenuItem(
    val id: String, val title: Int, val icon: ImageVector, val route: String
)

private val staticMenuItems = listOf(
    MenuItem("phonetics", R.string.phonetics, Icons.Default.RecordVoiceOver, "phonetics"),
    MenuItem("punctuation", R.string.punctuation, Icons.Default.EditNote, "punctuation"),
    MenuItem("settings", R.string.settings, Icons.Default.Settings, "settings")
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            EditorialMoment(
                onStartLearningClick = { navController.navigate("word_building/1") },
            )
        }

        item {
            LevelSelection(
                title = stringResource(R.string.word_building),
                icon = Icons.Default.Construction,
                numLevels = uiState.wordLevelCount,
                progressMap = uiState.userProgress.wordLevelsProgress,
                onLevelClick = { level -> navController.navigate("word_building/$level") }
            )
        }

        item {
            LevelSelection(
                title = stringResource(R.string.sentence_reading),
                icon = Icons.AutoMirrored.Filled.ChromeReaderMode,
                numLevels = uiState.sentenceLevelCount,
                progressMap = uiState.userProgress.sentenceLevelsProgress,
                onLevelClick = { level -> navController.navigate("sentence_reading/$level") }
            )
        }

        items(items = staticMenuItems, key = { it.id }) { item ->
            StaticMenuItemCard(
                item = item,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

@Composable
private fun LevelSelection(
    title: String,
    icon: ImageVector,
    numLevels: Int,
    progressMap: Map<Int, Int>,
    onLevelClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp))
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(visible = expanded) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    contentPadding = PaddingValues(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp) // Avoid excessive height
                ) {
                    items(numLevels) { level ->
                        val levelNum = level + 1
                        val progress = progressMap[levelNum] ?: 0
                        LevelCard(
                            level = levelNum,
                            progress = progress,
                            onClick = { onLevelClick(levelNum) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: Int, progress: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxSize(0.8f),
                strokeWidth = 4.dp
            )
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StaticMenuItemCard(
    item: MenuItem, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = item.icon, contentDescription = stringResource(item.title), modifier = Modifier.size(40.dp))
            Text(text = stringResource(item.title), style = MaterialTheme.typography.titleLarge)
        }
    }
}