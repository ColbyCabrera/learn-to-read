package com.example.readingfoundations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.data.Curriculum

@Composable
fun UnitsScreen(onUnitClick: (String) -> Unit) {
    LazyColumn {
        items(Curriculum.units) { unit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onUnitClick(unit.id) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = unit.title)
                }
            }
        }
    }
}