package com.example.readingfoundations.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readingfoundations.R
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.ui.AppViewModelProvider
import kotlin.random.Random
import com.example.readingfoundations.data.models.Unit as DataUnit

private data class MenuItem(
    val id: String, val title: Int, val icon: ImageVector, val route: String
)

private val staticMenuItems = listOf(
    MenuItem("phonetics", R.string.phonetics, Icons.Default.RecordVoiceOver, "phonetics/1"),
    MenuItem("punctuation", R.string.punctuation, Icons.Default.EditNote, "punctuation/1"),
    MenuItem(
        "reading_comprehension",
        R.string.reading_comprehension,
        Icons.AutoMirrored.Filled.MenuBook,
        "reading_comprehension"
    ),
    MenuItem("settings", R.string.settings, Icons.Default.Settings, "settings")
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.uiState.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(stringResource(R.string.home), stringResource(R.string.subjects))
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
        when (selectedItem) {
            0 -> UnitPathScreen(
                paddingValues = paddingValues,
                units = homeUiState.units,
                onUnitClick = { subject, level ->
                    val route = when (subject) {
                        com.example.readingfoundations.data.Subjects.PHONETICS -> "phonetics/$level"
                        com.example.readingfoundations.data.Subjects.WORD_BUILDING -> "reading_word/$level"
                        com.example.readingfoundations.data.Subjects.SENTENCE_READING -> "sentence_reading/$level"
                        com.example.readingfoundations.data.Subjects.PUNCTUATION -> "punctuation/$level"
                        else -> ""
                    }
                    if (route.isNotEmpty()) {
                        navController.navigate(route)
                    }
                }
            )
            1 -> SubjectsScreen(
                paddingValues = paddingValues,
                homeUiState = homeUiState,
                navController = navController
            )
        }
    }
}

@Composable
fun UnitPathScreen(
    paddingValues: PaddingValues,
    units: List<DataUnit>,
    onUnitClick: (String, Int) -> Unit
) {
    val shapes = with(MaterialTheme.shapes) {
        listOf(extraSmall, small, medium, large, extraLarge)
    }
    val unitShapes = remember(units.size) {
        units.map { shapes.random(Random(it.id)) }
    }

    val currentUnitIndex = units.indexOfFirst { it.progress < 1.0f }.let { if (it == -1) units.size else it }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(units) { index, unit ->
            UnitPathItem(
                unit = unit,
                shape = unitShapes[index],
                isCurrent = index == currentUnitIndex,
                isCompleted = index < currentUnitIndex,
                isFirst = index == 0,
                isLast = index == units.size - 1,
                onUnitClick = onUnitClick
            )
        }
    }
}

fun getNextLevel(unit: DataUnit): Level? {
    val subjectOrder = com.example.readingfoundations.data.Subjects.ALL
    val sortedLevels = unit.levels.sortedWith(
        compareBy<Level> { it.levelNumber }
            .thenBy { subjectOrder.indexOf(it.subject) }
    )
    return sortedLevels.firstOrNull { !it.isCompleted }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitPathItem(
    unit: DataUnit,
    shape: Shape,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onUnitClick: (String, Int) -> Unit
) {
    val nextLevel = remember(unit) { getNextLevel(unit) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            UnitShape(
                unit = unit,
                shape = shape,
                isCurrent = isCurrent,
                isCompleted = isCompleted,
                onClick = {
                    if (isCurrent && nextLevel != null) {
                        onUnitClick(nextLevel.subject, nextLevel.levelNumber)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        UnitPathNode(isCompleted = isCompleted, isFirst = isFirst, isLast = isLast)
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (nextLevel != null && isCurrent) {
                InfoBox(level = nextLevel)
            }
        }
    }
}

@Composable
fun UnitPathNode(isCompleted: Boolean, isFirst: Boolean, isLast: Boolean) {
    val lineColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val nodeColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .width(16.dp)
            .fillMaxHeight()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topColor = if (isCompleted && !isFirst) lineColor else Color.Transparent
            val bottomColor = if (isLast) Color.Transparent else lineColor
            drawLine(
                color = topColor,
                start = Offset(center.x, 0f),
                end = center,
                strokeWidth = 4.dp.toPx()
            )
            drawCircle(
                color = nodeColor,
                radius = 8.dp.toPx(),
                center = center
            )
            drawLine(
                color = bottomColor,
                start = center,
                end = Offset(center.x, size.height),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitShape(
    unit: DataUnit,
    shape: Shape,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(shape)
            .clickable(enabled = isCurrent, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isCurrent) {
                    CircularWavyProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize(0.8f),
                        color = contentColor
                    )
                }
                Text(
                    text = "Unit ${unit.id}",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun InfoBox(level: Level) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = level.subject, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Level ${level.levelNumber}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


// The old screen, adapted for the "Subjects" tab
@Composable
fun SubjectsScreen(
    paddingValues: PaddingValues,
    homeUiState: HomeUiState,
    navController: NavController
) {
    val wordLevelCount = remember(homeUiState.units) { homeUiState.units.flatMap { it.levels }.filter { it.subject == com.example.readingfoundations.data.Subjects.WORD_BUILDING }.maxOfOrNull { it.levelNumber } ?: 0 }
    val sentenceLevelCount = remember(homeUiState.units) { homeUiState.units.flatMap { it.levels }.filter { it.subject == com.example.readingfoundations.data.Subjects.SENTENCE_READING }.maxOfOrNull { it.levelNumber } ?: 0 }

    val wordProgressMap = remember(homeUiState.userProgress) { homeUiState.userProgress.completedLevels[com.example.readingfoundations.data.Subjects.WORD_BUILDING]?.associateWith { 100 } ?: emptyMap() }
    val sentenceProgressMap = remember(homeUiState.userProgress) { homeUiState.userProgress.completedLevels[com.example.readingfoundations.data.Subjects.SENTENCE_READING]?.associateWith { 100 } ?: emptyMap() }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            LevelSelection(
                title = stringResource(R.string.word_building),
                icon = Icons.Default.Construction,
                numLevels = wordLevelCount,
                progressMap = wordProgressMap,
                onLevelClick = { level -> navController.navigate("reading_word/$level") })
        }

        item {
            LevelSelection(
                title = stringResource(R.string.sentence_reading),
                icon = Icons.AutoMirrored.Filled.ChromeReaderMode,
                numLevels = sentenceLevelCount,
                progressMap = sentenceProgressMap,
                onLevelClick = { level -> navController.navigate("sentence_reading/$level") })
        }

        items(items = staticMenuItems, key = { it.id }) { item ->
            StaticMenuItemCard(
                item = item, onClick = { navController.navigate(item.route) })
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
                    modifier = Modifier.heightIn(max = 300.dp)
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