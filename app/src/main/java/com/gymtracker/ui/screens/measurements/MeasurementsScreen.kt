package com.gymtracker.ui.screens.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.data.entities.BodyMeasurement
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MeasurementsScreen(viewModel: MeasurementsViewModel = hiltViewModel()) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vücut Ölçümleri", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ölçüm Ekle")
            }
        }

        measurements.firstOrNull()?.let { latest ->
            Spacer(Modifier.height(16.dp))
            LatestMeasurementCard(latest)
        }

        Spacer(Modifier.height(16.dp))

        if (measurements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Straighten, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Henüz ölçüm yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) { Text("İlk Ölçümü Ekle") }
                }
            }
        } else {
            Text("Geçmiş", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(measurements) { m ->
                    MeasurementHistoryCard(m, onDelete = { viewModel.deleteMeasurement(m) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { viewModel.saveMeasurement(it); showAddDialog = false }
        )
    }
}

@Composable
private fun LatestMeasurementCard(m: BodyMeasurement) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Son Ölçüm", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                m.weightKg?.let { MeasurementBadge("Kilo", "$it kg") }
                m.bodyFatPercent?.let { MeasurementBadge("Yağ %", "$it%") }
                m.chestCm?.let { MeasurementBadge("Göğüs", "$it cm") }
                m.waistCm?.let { MeasurementBadge("Bel", "$it cm") }
            }
        }
    }
}

@Composable
private fun MeasurementBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MeasurementHistoryCard(m: BodyMeasurement, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(fmt.format(Date(m.date)), fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                val parts = buildList {
                    m.weightKg?.let { add("Kilo: $it kg") }
                    m.bodyFatPercent?.let { add("Yağ: $it%") }
                    m.waistCm?.let { add("Bel: $it cm") }
                    m.chestCm?.let { add("Göğüs: $it cm") }
                }
                Text(parts.joinToString(" · "), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddMeasurementDialog(
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurement) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var leftArm by remember { mutableStateOf("") }
    var rightArm by remember { mutableStateOf("") }
    var leftCalf by remember { mutableStateOf("") }
    var rightCalf by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ölçüm Ekle") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { MeasurementField("Kilo (kg)", weight) { weight = it } }
                item { MeasurementField("Vücut Yağ %", bodyFat) { bodyFat = it } }
                item { MeasurementField("Göğüs (cm)", chest) { chest = it } }
                item { MeasurementField("Bel (cm)", waist) { waist = it } }
                item { MeasurementField("Kalça (cm)", hips) { hips = it } }
                item { MeasurementField("Sol Kol (cm)", leftArm) { leftArm = it } }
                item { MeasurementField("Sağ Kol (cm)", rightArm) { rightArm = it } }
                item { MeasurementField("Sol Baldır (cm)", leftCalf) { leftCalf = it } }
                item { MeasurementField("Sağ Baldır (cm)", rightCalf) { rightCalf = it } }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(BodyMeasurement(
                    weightKg = weight.toFloatOrNull(),
                    bodyFatPercent = bodyFat.toFloatOrNull(),
                    chestCm = chest.toFloatOrNull(),
                    waistCm = waist.toFloatOrNull(),
                    hipsCm = hips.toFloatOrNull(),
                    leftArmCm = leftArm.toFloatOrNull(),
                    rightArmCm = rightArm.toFloatOrNull(),
                    leftCalfCm = leftCalf.toFloatOrNull(),
                    rightCalfCm = rightCalf.toFloatOrNull()
                ))
            }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
private fun MeasurementField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}
