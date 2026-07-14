@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.domain.ProgramExercise
import com.gymtracker.domain.ProgramExerciseSerializer

@Composable
fun ProgramsScreen(
    onProgramSelected: (Long) -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val programs by viewModel.programs.collectAsStateWithLifecycle()
    var showPurchaseDialog by remember { mutableStateOf<Program?>(null) }

    val freePrograms = programs.filter { !it.isPaid }
    val paidPrograms = programs.filter { it.isPaid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Programlar", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Antrenman programları", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (freePrograms.isNotEmpty()) {
                item {
                    Text("Ücretsiz Programlar", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium)
                }
                items(freePrograms) { program ->
                    ProgramCard(program, onClick = { onProgramSelected(program.id) }, onPurchase = {})
                }
            }
            if (paidPrograms.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Premium Programlar", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium)
                }
                items(paidPrograms) { program ->
                    ProgramCard(
                        program = program,
                        onClick = { onProgramSelected(program.id) },
                        onPurchase = { showPurchaseDialog = it }
                    )
                }
            }
        }
    }

    showPurchaseDialog?.let { program ->
        PurchaseDialog(
            program = program,
            onDismiss = { showPurchaseDialog = null },
            onConfirm = {
                viewModel.purchaseProgram(program)
                showPurchaseDialog = null
            }
        )
    }
}

@Composable
private fun ProgramCard(program: Program, onClick: () -> Unit, onPurchase: (Program) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(program.name, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium)
                    Text("by ${program.creatorName}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
                if (program.isPaid && !program.isPurchased) {
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Text("${program.price.toInt()}₺", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                } else if (program.isPurchased) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(program.description, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgramTag("${program.durationWeeks} hafta", Icons.Default.DateRange)
                ProgramTag("${program.daysPerWeek} gün/hafta", Icons.Default.FitnessCenter)
                DifficultyTag(program.difficulty)
            }

            if (program.isPaid && !program.isPurchased) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onPurchase(program) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Satın Al - ${program.price.toInt()}₺")
                }
            }
        }
    }
}

@Composable
private fun ProgramTag(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DifficultyTag(difficulty: ProgramDifficulty) {
    val color = when (difficulty) {
        ProgramDifficulty.BEGINNER -> Color(0xFF4CAF50)
        ProgramDifficulty.INTERMEDIATE -> Color(0xFFFFC107)
        ProgramDifficulty.ADVANCED -> Color(0xFFF44336)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(difficulty.displayName, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProgramDetailScreen(
    onBack: () -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    var showPurchaseDialog by remember { mutableStateOf<Program?>(null) }
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val program = state.program
    if (program == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Program bulunamadı", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onBack) { Text("Geri dön") }
            }
        }
        return
    }
    val workouts = state.workouts
    val locked = program.isPaid && !program.isPurchased

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(program.name, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatItem("Süre", "${program.durationWeeks} hafta")
                            StatItem("Gün/Hafta", "${program.daysPerWeek} gün")
                            StatItem("Seviye", program.difficulty.displayName)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(program.description, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text("Hazırlayan: ${program.creatorName}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (locked) {
                item {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Premium Program", fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("Antrenman planını görmek için satın alın",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = { showPurchaseDialog = program },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Satın Al - ${program.price.toInt()}₺",
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                item {
                    Column {
                        Text("Haftalık Antrenman Planı", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("Bu plan, programın ${program.durationWeeks} haftası boyunca tekrarlanır",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(workouts) { workout ->
                    WorkoutDayCard(workout)
                }
            }
        }
    }

    showPurchaseDialog?.let { selectedProgram ->
        PurchaseDialog(
            program = selectedProgram,
            onDismiss = { showPurchaseDialog = null },
            onConfirm = {
                viewModel.purchaseProgram(selectedProgram)
                showPurchaseDialog = null
            }
        )
    }
}

@Composable
private fun WorkoutDayCard(workout: ProgramWorkout) {
    var expanded by remember { mutableStateOf(false) }
    val exercises = remember(workout.exercises) { ProgramExerciseSerializer.fromJson(workout.exercises) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${workout.dayNumber}. Gün", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(workout.dayName, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("${exercises.size} hareket", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(8.dp))
                exercises.forEach { exercise -> ExerciseRow(exercise) }
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: ProgramExercise) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Text("${exercise.sets} set x ${exercise.reps}", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text("${exercise.restSeconds} sn", fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PurchaseDialog(program: Program, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programı Satın Al") },
        text = {
            Column {
                Text("\"${program.name}\" programını ${program.price.toInt()}₺ karşılığında satın almak istiyor musunuz?")
                Spacer(Modifier.height(8.dp))
                Text("Bu bir demo uygulamasıdır. Gerçek ödeme sistemi entegre edilmemiştir.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Satın Al (Demo)") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
