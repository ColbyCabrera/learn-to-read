package com.example.readingfoundations.ui.screens.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.ui.AppViewModelProvider
import com.example.readingfoundations.ui.screens.home.EditorialMoment
import com.example.readingfoundations.ui.screens.home.HomeViewModel

private data class MenuItem(
    val id: String, val title: Int, val icon: ImageVector, val route: String
)

private val staticMenuItems = listOf(
    MenuItem("phonetics", R.string.phonetics, Icons.Default.RecordVoiceOver, "phonetics"),
    MenuItem("punctuation", R.string.punctuation, Icons.Default.EditNote, "punctuation"),
    MenuItem(
        "reading_comprehension",
        R.string.reading_comprehension,
        Icons.AutoMirrored.Filled.MenuBook,
        "reading_comprehension"
    ),
    MenuItem("settings", R.string.settings, Icons.Default.Settings, "settings")
)

private val contentRoutes = listOf(
    "phonetics", "reading_word/1", "sentence_reading/1", "reading_comprehension"
)


@Composable
fun SectionsScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Subjects")
    val selectedIcons = listOf(R.drawable.home_filled_24px, R.drawable.school_filled_24px)
    val unselectedIcons = listOf(R.drawable.home_24px, R.drawable.school_24px)


    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(if (selectedItem == index) selectedIcons[index] else unselectedIcons[index]),
                                contentDescription = item,
                            )
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = paddingValues
        ) {
            item {
                EditorialMoment(
                    onStartLearningClick = { navController.navigate(contentRoutes.random()) },
                )
            }

            item {
                LevelSelection(
                    title = stringResource(R.string.word_building),
                    icon = Icons.Default.Construction,
                    numLevels = uiState.wordLevelCount,
                    progressMap = uiState.userProgress.wordLevelsProgress,
                    onLevelClick = { level -> navController.navigate("reading_word/$level") })
            }

            item {
                LevelSelection(
                    title = stringResource(R.string.sentence_reading),
                    icon = Icons.AutoMirrored.Filled.ChromeReaderMode,
                    numLevels = uiState.sentenceLevelCount,
                    progressMap = uiState.userProgress.sentenceLevelsProgress,
                    onLevelClick = { level -> navController.navigate("sentence_reading/$level") })
            }

            items(items = staticMenuItems, key = { it.id }) { item ->
                StaticMenuItemCard(
                    item = item, onClick = { navController.navigate(item.route) })
            }
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
        modifier = Modifier.fillMaxWidth(), onClick = { expanded = !expanded }) {
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
                            onClick = { onLevelClick(levelNum) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelCard(level: Int, progress: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f), onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = { progress.toFloat() / 100f },
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
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.title),
                modifier = Modifier.size(40.dp)
            )
            Text(text = stringResource(item.title), style = MaterialTheme.typography.titleLarge)
        }
    }
}