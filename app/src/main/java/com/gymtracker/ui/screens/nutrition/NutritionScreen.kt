@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.nutrition

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import kotlin.math.min

@Composable
fun NutritionScreen(viewModel: NutritionViewModel = hiltViewModel()) {
    val daily by viewModel.dailyNutrition.collectAsState()
    val foodItems by viewModel.foodItems.collectAsState()
    var showAddEntry by remember { mutableStateOf(false) }
    var showAddFood by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

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
            Text("Beslenme", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Row {
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FloatingActionButton(
                    onClick = { showAddEntry = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.Add, null) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Kalori özeti
        CalorieSummaryCard(daily)

        Spacer(Modifier.height(12.dp))

        // Makro çubukları
        MacroProgressCard(daily)

        Spacer(Modifier.height(16.dp))

        // Öğünler
        val grouped = daily.entries.groupBy { it.mealType }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.values().forEach { mealType ->
                val entries = grouped[mealType] ?: emptyList()
                if (entries.isNotEmpty()) {
                    item { MealSection(mealType, entries, onDelete = viewModel::deleteEntry) }
                }
            }
            item {
                TextButton(
                    onClick = { showAddFood = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Yeni Besin Ekle")
                }
            }
        }
    }

    if (showAddEntry) {
        AddEntryDialog(
            foodItems = foodItems,
            onDismiss = { showAddEntry = false },
            onConfirm = { item, amount, mealType ->
                viewModel.addEntry(item, amount, mealType)
                showAddEntry = false
            }
        )
    }

    if (showAddFood) {
        AddFoodItemDialog(
            onDismiss = { showAddFood = false },
            onConfirm = { viewModel.addCustomFood(it); showAddFood = false }
        )
    }

    if (showGoalDialog) {
        GoalDialog(
            current = daily.goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { viewModel.updateGoal(it); showGoalDialog = false }
        )
    }
}

@Composable
private fun CalorieSummaryCard(daily: DailyNutrition) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Günlük Kalori", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Text("${daily.totalCalories.toInt()}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(" / ${daily.goal.dailyCalories} kcal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(8.dp))
            val progress = min(daily.totalCalories / daily.goal.dailyCalories, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = StrokeCap.Round,
                color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text("${(daily.goal.dailyCalories - daily.totalCalories).toInt().coerceAtLeast(0)} kcal kaldı",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MacroProgressCard(daily: DailyNutrition) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MacroItem("Protein", daily.totalProtein, daily.goal.dailyProteinG.toFloat(),
                MaterialTheme.colorScheme.primary)
            MacroItem("Karb", daily.totalCarbs, daily.goal.dailyCarbsG.toFloat(),
                MaterialTheme.colorScheme.secondary)
            MacroItem("Yağ", daily.totalFat, daily.goal.dailyFatG.toFloat(),
                MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MacroItem(label: String, current: Float, goal: Float, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { min(current / goal, 1f) },
                modifier = Modifier.size(64.dp),
                color = color,
                strokeWidth = 6.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${current.toInt()}g", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Hedef: ${goal.toInt()}g", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MealSection(mealType: MealType, entries: List<FoodEntry>, onDelete: (FoodEntry) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(mealType.displayName, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Text("${entries.sumOf { it.calories.toDouble() }.toInt()} kcal",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.foodName, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("${entry.amountGrams.toInt()}g · P:${entry.protein.toInt()} K:${entry.carbs.toInt()} Y:${entry.fat.toInt()}",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${entry.calories.toInt()} kcal", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEntryDialog(
    foodItems: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem, Float, MealType) -> Unit
) {
    var selectedItem by remember { mutableStateOf(foodItems.firstOrNull()) }
    var amount by remember { mutableStateOf("100") }
    var selectedMeal by remember { mutableStateOf(MealType.BREAKFAST) }
    var expanded by remember { mutableStateOf(false) }
    var mealExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Öğün Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (foodItems.isEmpty()) {
                    Text("Önce besin ekleyin", color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Besin") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            foodItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} (${item.caloriesPer100g.toInt()} kcal/100g)") },
                                    onClick = { selectedItem = item; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Miktar (gram)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(expanded = mealExpanded, onExpandedChange = { mealExpanded = it }) {
                        OutlinedTextField(
                            value = selectedMeal.displayName, onValueChange = {}, readOnly = true,
                            label = { Text("Öğün") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(mealExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = mealExpanded, onDismissRequest = { mealExpanded = false }) {
                            MealType.values().forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal.displayName) },
                                    onClick = { selectedMeal = meal; mealExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                selectedItem?.let { onConfirm(it, amount.toFloatOrNull() ?: 100f, selectedMeal) }
            }, enabled = selectedItem != null) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
private fun AddFoodItemDialog(onDismiss: () -> Unit, onConfirm: (FoodItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Besin Ekle (100g başına)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Besin Adı") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = calories, onValueChange = { calories = it },
                    label = { Text("Kalori (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = protein, onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f))
                    OutlinedTextField(value = carbs, onValueChange = { carbs = it },
                        label = { Text("Karb (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fat, onValueChange = { fat = it },
                        label = { Text("Yağ (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onConfirm(FoodItem(
                    name = name,
                    caloriesPer100g = calories.toFloatOrNull() ?: 0f,
                    proteinPer100g = protein.toFloatOrNull() ?: 0f,
                    carbsPer100g = carbs.toFloatOrNull() ?: 0f,
                    fatPer100g = fat.toFloatOrNull() ?: 0f
                ))
            }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
private fun GoalDialog(
    current: com.gymtracker.data.entities.NutritionGoal,
    onDismiss: () -> Unit,
    onConfirm: (com.gymtracker.data.entities.NutritionGoal) -> Unit
) {
    var calories by remember { mutableStateOf(current.dailyCalories.toString()) }
    var protein by remember { mutableStateOf(current.dailyProteinG.toString()) }
    var carbs by remember { mutableStateOf(current.dailyCarbsG.toString()) }
    var fat by remember { mutableStateOf(current.dailyFatG.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Günlük Hedefler") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = calories, onValueChange = { calories = it },
                    label = { Text("Kalori Hedefi (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = protein, onValueChange = { protein = it },
                    label = { Text("Protein Hedefi (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = carbs, onValueChange = { carbs = it },
                    label = { Text("Karbonhidrat Hedefi (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fat, onValueChange = { fat = it },
                    label = { Text("Yağ Hedefi (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(com.gymtracker.data.entities.NutritionGoal(
                    dailyCalories = calories.toIntOrNull() ?: 2500,
                    dailyProteinG = protein.toIntOrNull() ?: 180,
                    dailyCarbsG = carbs.toIntOrNull() ?: 300,
                    dailyFatG = fat.toIntOrNull() ?: 80
                ))
            }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
