package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.R
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.ui.theme.ReadingFoundationsTheme
import com.example.readingfoundations.data.models.Unit as DataUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailsBottomSheet(
    unit: DataUnit, onLevelClick: (String, Int) -> Unit, onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        UnitDetailsContent(unit = unit, onLevelClick = onLevelClick)
    }
}

@Composable
private fun UnitDetailsContent(
    unit: DataUnit, onLevelClick: (String, Int) -> Unit, modifier: Modifier = Modifier
) {
    val sortedLevels = remember(unit) {
        unit.levels.sortedWith(compareBy<Level> { it.levelNumber }.thenBy { Subjects.ALL.indexOf(it.subject) })
    }

    // First incomplete level is the next one to play.
    val nextLevel = sortedLevels.firstOrNull { !it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.unit_title, unit.id),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
            items(sortedLevels) { level ->
                val isUnlocked = level.isCompleted || (nextLevel == level)

                ListItem(
                    headlineContent = {
                    Text(
                        text = stringResource(Subjects.getTitleRes(level.subject)),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        )
                    )
                }, supportingContent = {
                    Text(
                        text = stringResource(R.string.level_format, level.levelNumber),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f
                        )
                    )
                }, trailingContent = {
                    if (level.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.level_completed_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.level_locked_desc),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }, modifier = Modifier.then(
                    if (isUnlocked) {
                    Modifier.clickable {
                        onLevelClick(level.subject, level.levelNumber)
                    }
                } else {
                    Modifier
                }))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun UnitDetailsBottomSheetPreview() {
    val sampleUnit = DataUnit(
        id = 1, levels = listOf(
            Level(Subjects.PHONETICS, 1, true),
            Level(Subjects.WORD_BUILDING, 1, true),
            Level(Subjects.PHONETICS, 2, false),
            Level(Subjects.WORD_BUILDING, 2, false),
            Level(Subjects.SENTENCE_READING, 1, false)
        ), progress = 0.4f
    )
    ReadingFoundationsTheme {
        Surface {
            UnitDetailsContent(
                unit = sampleUnit, onLevelClick = { _, _ -> })
        }
    }
}
