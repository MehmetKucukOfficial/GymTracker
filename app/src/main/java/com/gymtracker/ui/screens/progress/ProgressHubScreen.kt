package com.gymtracker.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymtracker.ui.navigation.Screen

@Composable
fun ProgressHubScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "İlerleme",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gelişimini tek yerde takip et",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        ProgressDestinationCard(
            title = "Vücut Ölçümleri",
            description = "Kilo ve çevre ölçümlerindeki değişimi izle",
            icon = Icons.Default.Straighten,
            onClick = { onNavigate(Screen.Measurements.route) }
        )
        Spacer(Modifier.height(12.dp))
        ProgressDestinationCard(
            title = "İlerleme Fotoğrafları",
            description = "Dönemsel fotoğraflarını güvenli biçimde karşılaştır",
            icon = Icons.Default.PhotoLibrary,
            onClick = { onNavigate(Screen.ProgressPhotos.route) }
        )
        Spacer(Modifier.height(12.dp))
        ProgressDestinationCard(
            title = "Kas Yükü",
            description = "Son antrenmanların kas gruplarına etkisini gör",
            icon = Icons.Default.AccessibilityNew,
            onClick = { onNavigate(Screen.Fatigue.route) }
        )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Aç",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
