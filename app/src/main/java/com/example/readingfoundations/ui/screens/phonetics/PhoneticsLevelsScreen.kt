package com.example.readingfoundations.ui.screens.phonetics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RecordVoiceOver
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PhoneticsLevelsScreen(
    navController: NavController,
    viewModel: PhoneticsLevelsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.phonetics_levels)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
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
        ) {
            LevelSelection(
                title = stringResource(R.string.phonetics_practice),
                icon = Icons.Default.RecordVoiceOver,
                numLevels = uiState.levelCount,
                progressMap = uiState.progressMap,
                onLevelClick = { level ->
                    navController.navigate("phonetics/$level")
                }
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
                    contentDescription = stringResource(if (expanded) R.string.collapse else R.string.expand)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelCard(level: Int, progress: Int, onClick: () -> Unit) {
    val progressLambda = remember(progress) { { progress.toFloat() / 100f } }
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = progressLambda,
                modifier = Modifier.fillMaxSize(0.8f),
            )
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
