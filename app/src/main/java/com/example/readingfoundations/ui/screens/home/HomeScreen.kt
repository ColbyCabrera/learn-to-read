package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val route: String, val title: Int, val icon: ImageVector
)

private val menuItems = listOf(
    MenuItem("phonetics", R.string.phonetics, Icons.Default.RecordVoiceOver),
    MenuItem("word_building", R.string.word_building, Icons.Default.Construction),
    MenuItem("sentence_reading", R.string.sentence_reading, Icons.AutoMirrored.Filled.ChromeReaderMode),
    MenuItem("punctuation", R.string.punctuation, Icons.Default.EditNote),
    MenuItem("settings", R.string.settings, Icons.Default.Settings)
)

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by homeViewModel.homeUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EditorialMoment(
            onStartLearningClick = { navController.navigate(menuItems.filter { it.route != "settings" }.random().route) },
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(menuItems) { item ->
                val progress = when (item.route) {
                    "word_building" -> homeUiState.wordCompletionPercentage
                    "sentence_reading" -> homeUiState.sentenceCompletionPercentage
                    else -> null
                }
                MenuItemCard(
                    item = item,
                    progress = progress,
                    onClick = {
                        val destination = if (item.route == "word_building" || item.route == "sentence_reading") {
                            "level_selection/${item.route}"
                        } else {
                            item.route
                        }
                        navController.navigate(destination)
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    progress: Float?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.title),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(item.title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EditorialMoment(
    modifier: Modifier = Modifier,
    onStartLearningClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartLearningClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Learning")
        }
    }
}