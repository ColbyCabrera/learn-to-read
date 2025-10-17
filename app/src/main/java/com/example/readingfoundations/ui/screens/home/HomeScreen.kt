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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.readingfoundations.data.models.Unit
import com.example.readingfoundations.ui.AppViewModelProvider
import kotlin.random.Random

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

/**
 * Home screen composable that provides a two-tab layout ("Home" and "Subjects") and routes to content based on the selected tab.
 *
 * Displays a bottom navigation bar to switch between the UnitPath view and the Subjects view, observes UI state from the provided view model, and performs navigation to specific subject/level routes when a unit or level is selected.
 *
 * @param navController NavController used to navigate to subject and level destinations.
 * @param viewModel HomeViewModel that supplies the screen's UI state (units and progress); a default instance is provided by the app's ViewModel factory.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.uiState.collectAsState()
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
        when (selectedItem) {
            0 -> UnitPathScreen(
                paddingValues = paddingValues,
                units = homeUiState.units,
                onUnitClick = { subject, level ->
                    val route = when (subject) {
                        "Phonetics" -> "phonetics/$level"
                        "Word Building" -> "reading_word/$level"
                        "Sentence Reading" -> "sentence_reading/$level"
                        "Punctuation" -> "punctuation/$level"
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

/**
 * Display a vertically scrolling list of unit path items that reflect each unit's progress and allow navigation to the next level.
 *
 * Each unit is rendered with a deterministic shape and receives flags indicating whether it is the current unit (first with progress < 1.0),
 * completed (before the current unit), the first, or the last. Tapping a current unit triggers navigation via [onUnitClick].
 *
 * @param paddingValues Window insets and scaffold padding to apply around the content.
 * @param units The ordered list of units to display; each unit's progress determines current/completed state.
 * @param onUnitClick Called when the user selects a unit's actionable entry. Receives the subject identifier and the level number to navigate to.
 */
@Composable
fun UnitPathScreen(
    paddingValues: PaddingValues,
    units: List<Unit>,
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

/**
 * Selects the next incomplete Level within the given Unit according to the app's progression order.
 *
 * @param unit The Unit whose levels will be examined.
 * @return The next incomplete Level, or `null` if every level in the unit is completed.
 */
fun getNextLevel(unit: Unit): Level? {
    val subjectOrder = listOf("Phonetics", "Word Building", "Sentence Reading", "Punctuation")
    val sortedLevels = unit.levels.sortedWith(
        compareBy(
            { it.levelNumber % 2 },
            { subjectOrder.indexOf(it.subject) }
        )
    )
    return sortedLevels.firstOrNull { !it.isCompleted }
}


/**
 * Renders a single row in the unit path showing the unit card, a vertical progress node, and an optional info box.
 *
 * @param unit The unit model to display.
 * @param shape The shape applied to the unit card.
 * @param isCurrent True when this unit is the current active unit.
 * @param isCompleted True when this unit is completed.
 * @param isFirst True when this unit is the first item in the list.
 * @param isLast True when this unit is the last item in the list.
 * @param onUnitClick Callback invoked with a subject and level number to navigate into a level; called when the unit is current and a next level exists.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitPathItem(
    unit: Unit,
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

/**
 * Renders a vertical path node with connector lines whose appearance reflects completion and position.
 *
 * The node is drawn as a circle; the top connector is shown only when `isCompleted` is true and `isFirst` is false,
 * and the bottom connector is omitted when `isLast` is true. Completed state uses the theme primary color;
 * pending state uses the theme surfaceVariant color.
 *
 * @param isCompleted Whether the node and its preceding connector should appear in the completed state.
 * @param isFirst Whether this node is the first in the path (suppresses the top connector).
 * @param isLast Whether this node is the last in the path (suppresses the bottom connector).
 */
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

/**
 * Displays a shaped card representing a unit with visual states for current or completed and an optional click action.
 *
 * The card shows the unit label and, when `isCurrent` is true, an animated circular progress indicator reflecting `unit.progress`.
 *
 * @param unit The unit model (provides `id` for the label and `progress` for the indicator).
 * @param shape The shape used to clip and draw the card container.
 * @param isCurrent If true, highlights the card as the active unit and enables click interaction and progress display.
 * @param isCompleted If true, styles the card with the completed color scheme.
 * @param onClick Callback invoked when the card is clicked; only triggered when `isCurrent` is true.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitShape(
    unit: Unit,
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

/**
 * Shows a small card presenting the level's subject and its level number.
 *
 * @param level The `Level` whose subject and level number are displayed.
 */
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


/**
 * Renders the "Subjects" screen: level selectors for Word Building and Sentence Reading and a list of static menu items.
 *
 * The screen computes the available level counts and completed-level progress for each subject from the provided UI state,
 * then presents two expandable LevelSelection sections (Word Building and Sentence Reading) and a list of static menu items
 * that navigate via the provided NavController when selected.
 *
 * @param paddingValues Window/content padding to apply around the screen content.
 * @param homeUiState Current home UI state containing units and user progress used to derive level counts and progress maps.
 */
@Composable
fun SubjectsScreen(
    paddingValues: PaddingValues,
    homeUiState: HomeUiState,
    navController: NavController
) {
    val wordLevelCount = homeUiState.units.flatMap { it.levels }.filter { it.subject == "Word Building" }.maxOfOrNull { it.levelNumber } ?: 0
    val sentenceLevelCount = homeUiState.units.flatMap { it.levels }.filter { it.subject == "Sentence Reading" }.maxOfOrNull { it.levelNumber } ?: 0

    val wordProgressMap = homeUiState.userProgress.completedLevels["Word Building"]?.associateWith { 100 } ?: emptyMap()
    val sentenceProgressMap = homeUiState.userProgress.completedLevels["Sentence Reading"]?.associateWith { 100 } ?: emptyMap()


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

/**
 * Renders an expandable card that displays a grid of selectable level tiles for a category.
 *
 * When expanded, shows up to `numLevels` level tiles arranged in an adaptive grid; each tile
 * displays progress provided by `progressMap` and invokes `onLevelClick` with the selected level number.
 *
 * @param title The display title for the selection card.
 * @param icon The icon shown at the start of the card.
 * @param numLevels Total number of levels to render (levels are numbered starting at 1).
 * @param progressMap Map from level number to progress percentage (0–100); missing entries are treated as 0.
 * @param onLevelClick Callback invoked with the tapped level number.
 */
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