package com.example.readingfoundations.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
    var selectedUnit by remember { mutableStateOf<DataUnit?>(null) }

    val navigateToLevel: (String, Int) -> Unit = { subject, level ->
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
    }

    selectedUnit?.let { unit ->
        UnitDetailsBottomSheet(
            unit = unit,
            onLevelClick = { subject, level ->
                selectedUnit = null
                navigateToLevel(subject, level)
            },
            onDismiss = { selectedUnit = null }
        )
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            UnitPathScreen(
                paddingValues = paddingValues,
                homeUiState = homeUiState,
                onShowDetails = { unit -> selectedUnit = unit }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitPathScreen(
    paddingValues: PaddingValues,
    homeUiState: HomeUiState,
    onShowDetails: (DataUnit) -> Unit
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
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Your Journey",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

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
                onShowDetails = onShowDetails,
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
    onShowDetails: (DataUnit) -> Unit,
    nextLevel: Level?,
    nextIncompleteLevel: Int?
) {
    AnimatedVisibility(
        visible = true, // Simplified for entrance animation when composed, though proper state observation might be needed for a real entry. Let's rely on standard compose layout animation if needed, or just slide in.
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            initialOffsetY = { 50 }, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                UnitShape(
                    unit = unit,
                    shape = shape,
                    isCurrent = isCurrent,
                    isCompleted = isCompleted,
                    enabled = isCurrent || isCompleted,
                    onClick = {
                        onShowDetails(unit)
                    })
            }
            Spacer(modifier = Modifier.width(16.dp))
            UnitPathNode(isCompleted = isCompleted, isFirst = isFirst, isLast = isLast)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (nextLevel != null && isCurrent && nextIncompleteLevel != null) {
                    InfoBox(subject = nextLevel.subject, levelNumber = nextIncompleteLevel)
                }
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
            .width(24.dp)
            .fillMaxHeight()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topColor = if (isCompleted && !isFirst) lineColor else Color.Transparent
            val bottomColor = if (isLast) Color.Transparent else lineColor
            drawLine(
                color = topColor,
                start = Offset(center.x, 0f),
                end = center,
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = nodeColor, radius = 12.dp.toPx(), center = center
            )
            drawLine(
                color = bottomColor,
                start = center,
                end = Offset(center.x, size.height),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.tertiaryContainer
        isCompleted -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onTertiaryContainer
        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "PressScaleAnimation"
    )

    Box(
        modifier = Modifier
            .size(136.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        })
                }
            }, contentAlignment = Alignment.Center
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor, contentColor = contentColor
            ),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 12.dp else 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isCurrent) {
                    CircularProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize(0.6f),
                        color = MaterialTheme.colorScheme.onTertiary,
                        trackColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7F),
                        strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth * 2,
                    )
                }
                Text(
                    text = "${unit.id}",
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun InfoBox(subject: String, levelNumber: Int) {
    Card(
        modifier = Modifier.padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(
                    when (subject) {
                        Subjects.PHONETICS -> R.string.phonetics
                        Subjects.WORD_BUILDING -> R.string.word_building
                        Subjects.SENTENCE_READING -> R.string.sentence_reading
                        Subjects.PUNCTUATION -> R.string.punctuation
                        Subjects.READING_COMPREHENSION -> R.string.reading_comprehension
                        else -> R.string.subjects
                    }
                ), style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.level_format, levelNumber),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun getNextIncompleteLevel(subject: String, homeUiState: HomeUiState): Int {
    val completedLevels =
        homeUiState.userProgress.completedLevels[subject]?.toHashSet() ?: emptySet()
    var level = 1
    while (level in completedLevels) {
        level++
    }
    return level
}
