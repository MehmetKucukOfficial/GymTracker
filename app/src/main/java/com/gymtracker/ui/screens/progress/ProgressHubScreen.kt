package com.gymtracker.ui.screens.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymtracker.R
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.navigation.Screen
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing

@Composable
fun ProgressHubScreen(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = GtSpacing.md),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = GtSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
    ) {
        item {
            GtSectionHeader(
                title = stringResource(R.string.progress_hub_title),
                supportingText = stringResource(R.string.progress_hub_subtitle),
                modifier = Modifier.padding(bottom = GtSpacing.sm)
            )
        }
        item {
            ProgressDestinationCard(
                title = stringResource(R.string.progress_hub_measurements),
                description = stringResource(R.string.progress_hub_measurements_support),
                icon = Icons.Default.Straighten,
                onClick = { onNavigate(Screen.Measurements.route) }
            )
        }
        item {
            ProgressDestinationCard(
                title = stringResource(R.string.progress_hub_photos),
                description = stringResource(R.string.progress_hub_photos_support),
                icon = Icons.Default.PhotoLibrary,
                onClick = { onNavigate(Screen.ProgressPhotos.route) }
            )
        }
        item {
            ProgressDestinationCard(
                title = stringResource(R.string.progress_hub_load),
                description = stringResource(R.string.progress_hub_load_support),
                icon = Icons.Default.AccessibilityNew,
                onClick = { onNavigate(Screen.Fatigue.route) }
            )
        }
    }
}

@Composable
private fun ProgressDestinationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(GtSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(GtSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(GtSize.iconLarge),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.progress_hub_open),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
