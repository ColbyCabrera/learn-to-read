package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.R
import com.example.readingfoundations.data.Subjects
import com.example.readingfoundations.data.models.Level
import com.example.readingfoundations.data.models.Unit as DataUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailsBottomSheet(
    unit: DataUnit,
    onLevelClick: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedLevels = remember(unit) {
        unit.levels.sortedWith(
            compareBy<Level> { it.levelNumber }
                .thenBy { Subjects.ALL.indexOf(it.subject) }
        )
    }

    // First incomplete level is the next one to play.
    val nextLevel = sortedLevels.firstOrNull { !it.isCompleted }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.unit_title, unit.id),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(sortedLevels) { level ->
                    val isUnlocked = level.isCompleted || (nextLevel == level)

                    ListItem(
                        headlineContent = {
                            Text(
                                text = getSubjectTitle(level.subject),
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.level_format, level.levelNumber),
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        },
                        trailingContent = {
                            if (level.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        },
                        modifier = Modifier.then(
                            if (isUnlocked) {
                                Modifier.clickable {
                                    onLevelClick(level.subject, level.levelNumber)
                                    onDismiss()
                                }
                            } else {
                                Modifier
                            }
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun getSubjectTitle(subject: String): String {
    return stringResource(
        when (subject) {
            Subjects.PHONETICS -> R.string.phonetics
            Subjects.WORD_BUILDING -> R.string.word_building
            Subjects.SENTENCE_READING -> R.string.sentence_reading
            Subjects.PUNCTUATION -> R.string.punctuation
            Subjects.READING_COMPREHENSION -> R.string.reading_comprehension
            else -> R.string.subjects
        }
    )
}
