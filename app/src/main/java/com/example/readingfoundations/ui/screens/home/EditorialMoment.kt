package com.example.readingfoundations.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.readingfoundations.R

private object EditorialMomentDimens {
    val padding = 24.dp
    val cornerRadiusVisible = 24.dp
    val cornerRadiusHidden = 8.dp
    val spacer1 = 12.dp
    val spacer2 = 20.dp
    val buttonCorner = 12.dp
}

@Composable
fun EditorialMoment(
    onStartLearningClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring()
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isVisible) EditorialMomentDimens.cornerRadiusVisible else EditorialMomentDimens.cornerRadiusHidden,
        animationSpec = spring()
    )

    val animatedWeight by animateIntAsState(
        targetValue = if (isVisible) FontWeight.Bold.weight else FontWeight.Normal.weight,
        animationSpec = spring()
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(EditorialMomentDimens.padding),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.todays_learning_spotlight),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight(animatedWeight),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(EditorialMomentDimens.spacer1))
            Text(
                text = stringResource(R.string.spotlight_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(EditorialMomentDimens.spacer2))
            Button(
                onClick = onStartLearningClick,
                shape = RoundedCornerShape(EditorialMomentDimens.buttonCorner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(text = stringResource(R.string.start_learning))
            }
        }
    }
}