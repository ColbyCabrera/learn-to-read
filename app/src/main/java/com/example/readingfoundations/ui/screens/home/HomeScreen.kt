package com.example.readingfoundations.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.readingfoundations.R

private sealed class IconResource {
    data class Vector(val imageVector: ImageVector) : IconResource()
    data class Drawable(val id: Int) : IconResource()
}

private data class MenuItem(
    val route: String, val title: Int, val icon: IconResource
)

@Composable
fun HomeScreen(navController: NavController) {
    val menuItems = listOf(
        MenuItem(
            "phonetics",
            R.string.phonetics,
            IconResource.Drawable(R.drawable.record_voice_over_24px)
        ), MenuItem(
            "word_building",
            R.string.word_building,
            IconResource.Drawable(R.drawable.construction_24px)
        ), MenuItem(
            "sentence_reading",
            R.string.sentence_reading,
            IconResource.Drawable(R.drawable.book_24px)
        ), MenuItem(
            "punctuation", R.string.punctuation, IconResource.Drawable(R.drawable.edit_note_24px)
        ), MenuItem("settings", R.string.settings, IconResource.Vector(Icons.Default.Settings))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 96.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                MenuItemCard(item = item, onClick = { navController.navigate(item.route) })
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val icon = item.icon) {
                is IconResource.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                is IconResource.Drawable -> Icon(
                    painter = painterResource(id = icon.id),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = LocalContentColor.current
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(item.title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
