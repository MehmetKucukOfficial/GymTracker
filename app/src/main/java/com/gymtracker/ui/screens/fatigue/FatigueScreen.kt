package com.gymtracker.ui.screens.fatigue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.ui.components.MuscleBodyMap

@Composable
fun FatigueScreen(viewModel: FatigueViewModel = hiltViewModel()) {
    val fatigueData by viewModel.fatigueData.collectAsState()
    var showFront by remember { mutableStateOf(true) }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kas Bitkinlik Haritası",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Son 7 günlük antrenman verisi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Ön/Arka toggle
        SegmentedButton(showFront) {
            showFront = it
            selectedMuscle = null
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            MuscleBodyMap(
                fatigueData = fatigueData,
                showFront = showFront,
                selectedMuscle = selectedMuscle,
                onMuscleTap = { tapped ->
                    selectedMuscle = if (selectedMuscle == tapped) null else tapped
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            selectedMuscle?.let { muscle ->
                val fatigue = fatigueData.find { it.muscle == muscle }?.fatiguePercent ?: 0f
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "${muscle.displayName}: ${(fatigue * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedButton(showFront: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        listOf(true to "Ön", false to "Arka").forEach { (value, label) ->
            val selected = showFront == value
            Button(
                onClick = { onToggle(value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(label)
            }
        }
    }
}
