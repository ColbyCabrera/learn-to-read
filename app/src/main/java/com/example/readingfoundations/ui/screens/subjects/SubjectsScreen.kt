package com.example.readingfoundations.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.readingfoundations.R
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.ui.AppViewModelProvider

private data class MenuItem(
    val id: String, val title: Int, val icon: ImageVector, val route: String
)

private val staticMenuItems = listOf(
    MenuItem("settings", R.string.settings, Icons.Default.Settings, "settings")
)

/**
 * Renders the Subjects screen with a bottom navigation bar and level-based sections for subjects.
 *
 * Displays expandable LevelSelection cards for Phonetics, Word Building, and Sentence Reading using counts and user progress from the provided view model, followed by static menu items. Tapping a level navigates to the subject-specific route (for example, "phonetics/{level}", "reading_word/{level}", "sentence_reading/{level}"); the bottom navigation switches between "home" and "subjects" while preserving navigation state.
 *
 * @param navController NavController used for in-app navigation.
 * @param viewModel SubjectsViewModel providing UI state (level counts and user progress).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubjectsScreen(
    navController: NavController,
    viewModel: SubjectsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = listOf(stringResource(R.string.home), stringResource(R.string.subjects))
    val routes = listOf("home", "subjects")
    val selectedIcons = listOf(R.drawable.home_filled_24px, R.drawable.school_filled_24px)
    val unselectedIcons = listOf(R.drawable.home_24px, R.drawable.school_24px)
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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .statusBarsPadding(),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Explore Subjects",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = "Pick a topic and start learning!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                LevelSelectionTile(
                    title = stringResource(R.string.phonetics),
                    icon = Icons.Default.RecordVoiceOver,
                    numLevels = uiState.phoneticsLevelCount,
                    progressMap = uiState.userProgress.completedLevels[Subjects.PHONETICS]?.associateWith { 100 }
                        ?: emptyMap(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(
                        topStart = 48.dp,
                        bottomEnd = 48.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    heightDp = 180,
                    isHero = true,
                    onLevelClick = { level -> navController.navigate("phonetics/$level") })

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LevelSelectionTile(
                            title = stringResource(R.string.sentence_reading),
                            icon = Icons.AutoMirrored.Filled.ChromeReaderMode,
                            numLevels = uiState.sentenceLevelCount,
                            progressMap = uiState.userProgress.completedLevels[Subjects.SENTENCE_READING]?.associateWith { 100 }
                                ?: emptyMap(),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            shape = RoundedCornerShape(32.dp, 8.dp, 32.dp, 8.dp),
                            heightDp = 180,
                            onLevelClick = { level -> navController.navigate("sentence_reading/$level") })

                        LevelSelectionTile(
                            title = stringResource(R.string.reading_comprehension),
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            numLevels = uiState.readingComprehensionLevelCount,
                            progressMap = uiState.userProgress.completedLevels[Subjects.READING_COMPREHENSION]?.associateWith { 100 }
                                ?: emptyMap(),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(24.dp),
                            heightDp = 220,
                            onLevelClick = { level -> navController.navigate("reading_comprehension/$level") })
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LevelSelectionTile(
                            title = stringResource(R.string.word_building),
                            icon = Icons.Default.Construction,
                            numLevels = uiState.wordLevelCount,
                            progressMap = uiState.userProgress.completedLevels[Subjects.WORD_BUILDING]?.associateWith { 100 }
                                ?: emptyMap(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(16.dp, 48.dp, 16.dp, 48.dp),
                            heightDp = 150,
                            onLevelClick = { level -> navController.navigate("reading_word/$level") })

                        LevelSelectionTile(
                            title = stringResource(R.string.punctuation),
                            icon = Icons.Default.EditNote,
                            numLevels = uiState.punctuationLevelCount,
                            progressMap = uiState.userProgress.completedLevels[Subjects.PUNCTUATION]?.associateWith { 100 }
                                ?: emptyMap(),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(48.dp, 16.dp, 48.dp, 16.dp),
                            heightDp = 150,
                            onLevelClick = { level -> navController.navigate("punctuation/$level") })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                staticMenuItems.forEach { item ->
                    StaticMenuItemCard(
                        item = item, onClick = { navController.navigate(item.route) })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelSelectionTile(
    title: String,
    icon: ImageVector,
    numLevels: Int,
    progressMap: Map<Int, Int>,
    containerColor: Color,
    contentColor: Color,
    shape: androidx.compose.ui.graphics.Shape,
    heightDp: Int,
    isHero: Boolean = false,
    onLevelClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "TilePressScale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (expanded) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TileElevation"
    )

    val completedCount = progressMap.count { it.value >= 100 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = heightDp.dp)
            .animateContentSize(spring(dampingRatio = 0.7f, stiffness = 200f))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        expanded = !expanded
                    })
            }) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor, contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Watermark icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.10f),
                    modifier = Modifier
                        .size(if (isHero) 160.dp else 120.dp)
                        .align(Alignment.BottomEnd)
                        .graphicsLayer {
                            translationX = 32.dp.toPx()
                            translationY = 32.dp.toPx()
                        }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isHero) 24.dp else 16.dp),
                    verticalArrangement = if (!expanded) Arrangement.Center else Arrangement.Top,
                    horizontalAlignment = if (isHero) Alignment.Start else Alignment.CenterHorizontally
                ) {
                    if (isHero) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(if (expanded) 32.dp else 48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = if (expanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                )
                                if (!expanded && numLevels > 0) {
                                    Text(
                                        text = "$completedCount / $numLevels completed",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = contentColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (expanded) 32.dp else 48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = if (expanded) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (!expanded && numLevels > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount / $numLevels",
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = if (isHero) Arrangement.Start else Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (level in 0 until numLevels) {
                                val levelNum = level + 1
                                val progress = progressMap[levelNum] ?: 0
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .padding(4.dp)
                                ) {
                                    LevelCard(
                                        level = levelNum,
                                        progress = progress,
                                        contentColor = contentColor,
                                        onClick = { onLevelClick(levelNum) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelCard(level: Int, progress: Int, contentColor: Color, onClick: () -> Unit) {
    val isComplete = progress >= 100
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete)
                contentColor.copy(alpha = 0.20f)
            else
                Color.White.copy(alpha = 0.12f),
            contentColor = LocalContentColor.current
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            1.5f.dp,
            if (isComplete) contentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            } else {
                CircularWavyProgressIndicator(
                    progress = { progress.toFloat() / 100f },
                    modifier = Modifier.fillMaxSize(0.75f),
                )
            }
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = stringResource(item.title),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(item.title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
