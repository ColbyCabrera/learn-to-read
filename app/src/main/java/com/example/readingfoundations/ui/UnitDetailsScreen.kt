package com.example.readingfoundations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.data.ContentType
import com.example.readingfoundations.data.Curriculum

@Composable
fun UnitDetailsScreen(
    unitId: String,
    onLevelClick: (ContentType, Int) -> Unit
) {
    val unit = Curriculum.units.find { it.id == unitId }
    if (unit != null) {
        LazyColumn {
            unit.levels.forEach { (contentType, levels) ->
                item {
                    Text(
                        text = contentType.title,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(levels) { level ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onLevelClick(contentType, level.difficulty) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Level ${level.levelNumber}")
                        }
                    }
                }
            }
        }
    } else {
        Text("Unit not found")
    }
}