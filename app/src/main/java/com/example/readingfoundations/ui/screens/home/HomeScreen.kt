package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.readingfoundations.R
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.ui.AppViewModelProvider
import kotlin.random.Random
import com.example.readingfoundations.data.models.Unit as DataUnit

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.uiState.collectAsState()
    val items = listOf(stringResource(R.string.home), stringResource(R.string.subjects))
    val selectedIcons = listOf(R.drawable.home_filled_24px, R.drawable.school_filled_24px)
    val unselectedIcons = listOf(R.drawable.home_24px, R.drawable.school_24px)
    val routes = listOf("home", "subjects")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedItem = routes.indexOf(currentRoute)

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
                        onClick = {
                            navController.navigate(routes[index]) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        UnitPathScreen(
            paddingValues = paddingValues,
            homeUiState = homeUiState,
            onUnitClick = { subject, level ->
                val route = when (subject) {
                    Subjects.PHONETICS -> "phonetics/$level"
                    Subjects.WORD_BUILDING -> "reading_word/$level"
                    Subjects.SENTENCE_READING -> "sentence_reading/$level"
                    Subjects.PUNCTUATION -> "punctuation/$level"
                    Subjects.READING_COMPREHENSION -> "reading_comprehension/$level"
                    else -> ""
                }
                if (route.isNotEmpty()) {
                    navController.navigate(route)
                }
            })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitPathScreen(
    paddingValues: PaddingValues, homeUiState: HomeUiState, onUnitClick: (String, Int) -> Unit
) {
    val units = homeUiState.units
    val shapes = with(MaterialShapes) {
        listOf(
            Clover8Leaf,
            Cookie6Sided,
            Cookie7Sided,
            Cookie9Sided,
            Cookie12Sided,
            Flower,
            Puffy,
            PuffyDiamond,
            SoftBoom,
            SoftBurst,
            Sunny,
            VerySunny
        )
    }
    val unitShapes = remember(units.map { it.id }) {
        units.associate { it.id to shapes.random(Random(it.id)) }
    }

    val currentUnitIndex =
        units.indexOfFirst { it.progress < 1.0f }.let { if (it == -1) units.size else it }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(units, key = { _, unit -> unit.id }) { index, unit ->
            val nextLevel by remember(unit) { derivedStateOf { getNextLevel(unit) } }
            val nextIncompleteLevel by remember(nextLevel, homeUiState) {
                derivedStateOf {
                    nextLevel?.let {
                        getNextIncompleteLevel(it.subject, homeUiState)
                    }
                }
            }
            UnitPathItem(
                unit = unit,
                shape = unitShapes[unit.id]?.toShape() ?: MaterialTheme.shapes.medium,
                isCurrent = index == currentUnitIndex,
                isCompleted = index < currentUnitIndex,
                isFirst = index == 0,
                isLast = index == units.size - 1,
                onUnitClick = onUnitClick,
                nextLevel = nextLevel,
                nextIncompleteLevel = nextIncompleteLevel
            )
        }
    }
}

fun getNextLevel(unit: DataUnit): Level? {
    val subjectOrder = Subjects.ALL
    val sortedLevels =
        unit.levels.sortedWith(compareBy<Level> { it.levelNumber }.thenBy { subjectOrder.indexOf(it.subject) })
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
    onUnitClick: (String, Int) -> Unit,
    nextLevel: Level?,
    nextIncompleteLevel: Int?
) {
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
                })
        }
        Spacer(modifier = Modifier.width(16.dp))
        UnitPathNode(isCompleted = isCompleted, isFirst = isFirst, isLast = isLast)
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (nextLevel != null && isCurrent && nextIncompleteLevel != null) {
                InfoBox(level = Level(nextLevel.subject, nextIncompleteLevel, false))
            }
        }
    }
}

@Composable
fun UnitPathNode(isCompleted: Boolean, isFirst: Boolean, isLast: Boolean) {
    val lineColor =
        if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val nodeColor =
        if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                color = nodeColor, radius = 8.dp.toPx(), center = center
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
    unit: DataUnit, shape: Shape, isCurrent: Boolean, isCompleted: Boolean, onClick: () -> Unit
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
            .clickable(enabled = isCurrent, onClick = onClick), contentAlignment = Alignment.Center
    ) {
        Card(
            shape = shape, colors = CardDefaults.cardColors(
                containerColor = containerColor, contentColor = contentColor
            ), modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isCurrent) {
                    CircularProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize(0.55f),
                        color = contentColor,
                        strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth * 2,
                    )
                }
                Text(
                    text = "${unit.id}",
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
        modifier = Modifier.padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(
                    when (level.subject) {
                        Subjects.PHONETICS -> R.string.phonetics
                        Subjects.WORD_BUILDING -> R.string.word_building
                        Subjects.SENTENCE_READING -> R.string.sentence_reading
                        Subjects.PUNCTUATION -> R.string.punctuation
                        Subjects.READING_COMPREHENSION -> R.string.reading_comprehension
                        else -> R.string.subjects
                    }
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = stringResource(R.string.level_format, level.levelNumber), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun getNextIncompleteLevel(subject: String, homeUiState: HomeUiState): Int {
    val completedLevels = homeUiState.userProgress.completedLevels[subject]?.toHashSet() ?: emptySet()
    var level = 1
    while (level in completedLevels) {
        level++
    }
    return level
}
