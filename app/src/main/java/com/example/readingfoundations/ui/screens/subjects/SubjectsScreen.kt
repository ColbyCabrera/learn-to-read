package com.example.readingfoundations.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            contentPadding = paddingValues
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Explore Subjects",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                LevelSelectionTile(
                    title = stringResource(R.string.phonetics),
                    icon = Icons.Default.RecordVoiceOver,
                    numLevels = uiState.phoneticsLevelCount,
                    progressMap = uiState.userProgress.completedLevels[Subjects.PHONETICS]?.associateWith { 100 }
                        ?: emptyMap(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(32.dp),
                    heightDp = 160,
                    onLevelClick = { level -> navController.navigate("phonetics/$level") })
            }
            item {
                LevelSelectionTile(
                    title = stringResource(R.string.word_building),
                    icon = Icons.Default.Construction,
                    numLevels = uiState.wordLevelCount,
                    progressMap = uiState.userProgress.completedLevels[Subjects.WORD_BUILDING]?.associateWith { 100 }
                        ?: emptyMap(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(16.dp, 48.dp, 16.dp, 48.dp),
                    heightDp = 200,
                    onLevelClick = { level -> navController.navigate("reading_word/$level") })
            }

            item {
                LevelSelectionTile(
                    title = stringResource(R.string.sentence_reading),
                    icon = Icons.AutoMirrored.Filled.ChromeReaderMode,
                    numLevels = uiState.sentenceLevelCount,
                    progressMap = uiState.userProgress.completedLevels[Subjects.SENTENCE_READING]?.associateWith { 100 }
                        ?: emptyMap(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    heightDp = 180,
                    onLevelClick = { level -> navController.navigate("sentence_reading/$level") })
            }

            item {
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

            item {
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

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(items = staticMenuItems, key = { it.id }) { item ->
                StaticMenuItemCard(
                    item = item, onClick = { navController.navigate(item.route) })
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
    onLevelClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "TilePressScale"
    )

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
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 8.dp else 2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                verticalArrangement = if (!expanded) Arrangement.Center else Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (expanded) 32.dp else 48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = if (expanded) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(400)),
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (level in 0 until numLevels) {
                            val levelNum = level + 1
                            val progress = progressMap[levelNum] ?: 0
                            Box(modifier = Modifier
                                .size(56.dp)
                                .padding(4.dp)) {
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
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelCard(level: Int, progress: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f), onClick = onClick, colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = LocalContentColor.current
        ), elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularWavyProgressIndicator(
                progress = { progress.toFloat() / 100f },
                modifier = Modifier.fillMaxSize(0.8f),
            )
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleMedium,
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
