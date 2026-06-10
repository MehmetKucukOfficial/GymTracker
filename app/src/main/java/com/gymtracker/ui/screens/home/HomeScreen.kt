package com.gymtracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Günaydın"
            hour < 17 -> "İyi Öğlenler"
            else -> "İyi Akşamlar"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(greeting, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("GymTracker'a hoş geldiniz", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(20.dp))

        // İstatistik kartları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Toplam Antrenman", "${stats.totalWorkouts}", Icons.Default.FitnessCenter,
                MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatCard("Bu Hafta", "${stats.weekWorkouts}", Icons.Default.CalendarToday,
                MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Kilo", stats.latestWeight?.let { "$it kg" } ?: "-",
                Icons.Default.Monitor, Color(0xFF9C27B0), Modifier.weight(1f))
            StatCard("Bugün Kalori", "${stats.todayCalories} kcal",
                Icons.Default.LocalFireDepartment, Color(0xFFFF5722), Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text("Hızlı Erişim", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(12.dp))

        // Hızlı erişim kartları
        val quickItems = listOf(
            Triple("Antrenman Başlat", Icons.Default.PlayArrow, Screen.Workout.route),
            Triple("Ölçüm Ekle", Icons.Default.Straighten, Screen.Measurements.route),
            Triple("Öğün Ekle", Icons.Default.Restaurant, Screen.Nutrition.route),
            Triple("Kas Haritası", Icons.Default.Accessibility, Screen.Fatigue.route),
            Triple("Fotoğraf Ekle", Icons.Default.AddPhotoAlternate, Screen.Progress.route),
            Triple("Programlar", Icons.Default.List, Screen.Programs.route)
        )

        quickItems.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (label, icon, route) ->
                    QuickAccessCard(label, icon, onClick = { onNavigate(route) },
                        modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Son antrenman
        stats.lastWorkoutName?.let { name ->
            Text("Son Antrenman", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FitnessCenter, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(name, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        stats.lastWorkoutDate?.let { date ->
                            Text(SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(date)),
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String, value: String, icon: ImageVector,
    color: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAccessCard(
    label: String, icon: ImageVector,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium)
        }
    }
}
