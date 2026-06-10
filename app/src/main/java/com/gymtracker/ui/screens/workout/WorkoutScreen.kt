@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.domain.ExerciseDefinition
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showNewSessionDialog by remember { mutableStateOf(false) }

    if (uiState.activeSession != null) {
        ActiveSessionScreen(viewModel)
        return
    }

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
            Text(
                "Antrenmanlar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            FloatingActionButton(
                onClick = { showNewSessionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Antrenman")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            EmptyWorkoutState { showNewSessionDialog = true }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions) { session ->
                    SessionCard(session) {
                        viewModel.loadSession(session.id)
                    }
                }
            }
        }
    }

    if (showNewSessionDialog) {
        NewSessionDialog(
            onDismiss = { showNewSessionDialog = false },
            onConfirm = { name ->
                viewModel.startSession(name)
                showNewSessionDialog = false
            }
        )
    }
}

@Composable
private fun EmptyWorkoutState(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text("Henüz antrenman yok", color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStart) { Text("İlk Antrenmanı Başlat") }
        }
    }
}

@Composable
private fun SessionCard(session: WorkoutSession, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("tr"))
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.name, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(fmt.format(Date(session.date)), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (session.durationMinutes > 0) {
                    Text("${session.durationMinutes} dakika", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NewSessionDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Antrenman") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Antrenman Adı") },
                placeholder = { Text("örn: Göğüs & Triceps") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Başlat") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
fun ActiveSessionScreen(viewModel: WorkoutViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.activeSession ?: return
    val sets = uiState.activeSets
    var showAddSetDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val groupedSets = sets.groupBy { it.exerciseName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(session.name, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Aktif Antrenman", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
            Button(
                onClick = { showFinishDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Bitir")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            groupedSets.forEach { (exerciseName, exerciseSets) ->
                item {
                    ExerciseSetGroup(exerciseName, exerciseSets, onDeleteSet = viewModel::deleteSet)
                }
            }
        }

        Button(
            onClick = { showAddSetDialog = true },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Set Ekle")
        }
    }

    if (showAddSetDialog) {
        AddSetDialog(
            categories = viewModel.getExerciseCategories(),
            exercisesForCategory = { viewModel.getExercisesForCategory(it) },
            onDismiss = { showAddSetDialog = false },
            onConfirm = { exercise, setNum, reps, weight ->
                viewModel.addSet(exercise, setNum, reps, weight)
                showAddSetDialog = false
            }
        )
    }

    if (showFinishDialog) {
        FinishSessionDialog(
            onDismiss = { showFinishDialog = false },
            onConfirm = { duration, notes ->
                viewModel.finishSession(duration, notes)
                showFinishDialog = false
            }
        )
    }
}

@Composable
private fun ExerciseSetGroup(
    exerciseName: String,
    sets: List<WorkoutSet>,
    onDeleteSet: (WorkoutSet) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(exerciseName, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Set", "Tekrar", "Ağırlık (kg)", "").forEach { header ->
                    Text(header, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f))
                }
            }
            sets.forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${set.setNumber}", modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("${set.reps}", modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("${set.weightKg} kg", modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface)
                    IconButton(
                        onClick = { onDeleteSet(set) },
                        modifier = Modifier.weight(1f).size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSetDialog(
    categories: List<String>,
    exercisesForCategory: (String) -> List<ExerciseDefinition>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, Float) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var exercisesInCategory by remember { mutableStateOf(exercisesForCategory(selectedCategory)) }
    var selectedExercise by remember { mutableStateOf(exercisesInCategory.firstOrNull()) }
    var setNumber by remember { mutableStateOf("1") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("0") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var exerciseExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kas Grubu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    exercisesInCategory = exercisesForCategory(category)
                                    selectedExercise = exercisesInCategory.firstOrNull()
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = exerciseExpanded, onExpandedChange = { exerciseExpanded = it }) {
                    OutlinedTextField(
                        value = selectedExercise?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Egzersiz") },
                        supportingText = selectedExercise?.let { ex -> { Text(ex.equipment) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exerciseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = exerciseExpanded, onDismissRequest = { exerciseExpanded = false }) {
                        exercisesInCategory.forEach { exercise ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(exercise.name)
                                        Text(exercise.equipment, fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { selectedExercise = exercise; exerciseExpanded = false }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setNumber, onValueChange = { setNumber = it },
                        label = { Text("Set") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps, onValueChange = { reps = it },
                        label = { Text("Tekrar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Ağırlık (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val exercise = selectedExercise ?: return@Button
                onConfirm(
                    exercise.name,
                    setNumber.toIntOrNull() ?: 1,
                    reps.toIntOrNull() ?: 0,
                    weight.toFloatOrNull() ?: 0f
                )
            }) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
private fun FinishSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var duration by remember { mutableStateOf("60") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Antrenmanı Bitir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = duration, onValueChange = { duration = it },
                    label = { Text("Süre (dakika)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notlar (opsiyonel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(duration.toIntOrNull() ?: 0, notes) }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
