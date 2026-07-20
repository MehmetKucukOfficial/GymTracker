@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.nutrition.isValidFoodAmount
import com.gymtracker.data.nutrition.isValidFoodItem
import com.gymtracker.data.nutrition.isValidNutritionGoal
import com.gymtracker.data.nutrition.nutritionProgress
import com.gymtracker.domain.parseLocalizedDecimal
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.components.GtSnackbarHost
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(viewModel: NutritionViewModel = hiltViewModel()) {
    val daily by viewModel.dailyNutrition.collectAsStateWithLifecycle()
    val foodItems by viewModel.foodItems.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val mutationError by viewModel.mutationError.collectAsStateWithLifecycle()
    val deletedEntry by viewModel.deletedEntry.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddEntry by remember { mutableStateOf(false) }
    var showAddFood by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<FoodEntry?>(null) }

    Box(Modifier.fillMaxSize()) {
        NutritionContent(
            daily = daily,
            selectedDate = selectedDate,
            canGoNext = selectedDate.isBefore(LocalDate.now()),
            onPreviousDay = viewModel::selectPreviousDay,
            onNextDay = viewModel::selectNextDay,
            onToday = viewModel::selectToday,
            onAddEntry = { showAddEntry = true },
            onGoalSettings = { showGoalDialog = true },
            onAddCustomFood = { showAddFood = true },
            onDeleteEntry = viewModel::deleteEntry,
            onEditEntry = { editingEntry = it }
        )
        GtSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    val deletedMessage = stringResource(R.string.nutrition_entry_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    LaunchedEffect(deletedEntry) {
        deletedEntry?.let { entry ->
            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(entry)
            else viewModel.consumeDeletedEntry(entry)
        }
    }

    if (showAddEntry) {
        AddEntryDialog(
            foodItems = foodItems,
            onDismiss = { showAddEntry = false },
            onConfirm = { item, amount, mealType ->
                viewModel.addEntry(item, amount, mealType) { showAddEntry = false }
            }
        )
    }
    if (showAddFood) {
        AddFoodItemDialog(
            onDismiss = { showAddFood = false },
            onConfirm = { item -> viewModel.addCustomFood(item) { showAddFood = false } }
        )
    }
    if (showGoalDialog) {
        GoalDialog(
            current = daily.goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goal -> viewModel.updateGoal(goal) { showGoalDialog = false } }
        )
    }
    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onConfirm = { amount, meal ->
                viewModel.updateEntry(entry, amount, meal) { editingEntry = null }
            }
        )
    }
    mutationError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearMutationError,
            title = { Text(stringResource(R.string.nutrition_title)) },
            text = { Text(error.userMessage()) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMutationError) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
fun NutritionContent(
    daily: DailyNutrition,
    selectedDate: LocalDate,
    canGoNext: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAddEntry: () -> Unit,
    onGoalSettings: () -> Unit,
    onAddCustomFood: () -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onEditEntry: (FoodEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val grouped = daily.entries.groupBy(FoodEntry::mealType)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md, vertical = GtSpacing.lg)
            .testTag("nutrition-content"),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GtSectionHeader(
                    title = stringResource(R.string.nutrition_title),
                    supportingText = stringResource(R.string.nutrition_subtitle),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onGoalSettings,
                    modifier = Modifier.size(GtSize.minimumTouchTarget)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.nutrition_goal_settings)
                    )
                }
                FloatingActionButton(
                    onClick = onAddEntry,
                    modifier = Modifier.size(GtSize.minimumTouchTarget)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nutrition_add_entry))
                }
            }
        }
        item {
            NutritionDateNavigator(
                selectedDate = selectedDate,
                canGoNext = canGoNext,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday
            )
        }
        item { CalorieSummaryCard(daily) }
        item { MacroProgressCard(daily) }
        if (daily.entries.isEmpty()) {
            item {
                GtEmptyState(
                    title = stringResource(R.string.nutrition_empty_title),
                    body = stringResource(R.string.nutrition_empty_body),
                    actionLabel = stringResource(R.string.nutrition_add_entry),
                    onAction = onAddEntry
                )
            }
        } else {
            MealType.values().forEach { mealType ->
                grouped[mealType]?.takeIf { it.isNotEmpty() }?.let { entries ->
                    item(key = mealType.name) {
                        MealSection(mealType, entries, onDeleteEntry, onEditEntry)
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = onAddCustomFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GtSize.minimumTouchTarget)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(GtSpacing.xs))
                Text(stringResource(R.string.nutrition_add_custom_food))
            }
        }
    }
}

@Composable
private fun NutritionDateNavigator(
    selectedDate: LocalDate,
    canGoNext: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", locale) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nutrition_previous_day)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedDate.format(formatter), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onToday) { Text(stringResource(R.string.nutrition_today)) }
            }
            IconButton(onClick = onNextDay, enabled = canGoNext) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.nutrition_next_day)
                )
            }
        }
    }
}

@Composable
private fun CalorieSummaryCard(daily: DailyNutrition) {
    val rawProgress = nutritionProgress(daily.totalCalories, daily.goal.dailyCalories)
    val overGoal = rawProgress > 1f
    val difference = (daily.totalCalories - daily.goal.dailyCalories).roundToInt()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (overGoal) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        ),
        modifier = Modifier.testTag(if (overGoal) "nutrition-calories-over" else "nutrition-calories-normal")
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Text(stringResource(R.string.nutrition_daily_calories), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.nutrition_calorie_value,
                    daily.totalCalories.roundToInt(),
                    daily.goal.dailyCalories
                ),
                style = MaterialTheme.typography.headlineSmall
            )
            LinearProgressIndicator(
                progress = { rawProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = StrokeCap.Round,
                color = if (overGoal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (overGoal) {
                    stringResource(R.string.nutrition_calories_over, difference)
                } else {
                    stringResource(R.string.nutrition_calories_remaining, -difference)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MacroProgressCard(daily: DailyNutrition) {
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
        ) {
            Text(stringResource(R.string.nutrition_macros), style = MaterialTheme.typography.titleMedium)
            if (largeFont) {
                Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.md)) {
                    MacroItem(stringResource(R.string.nutrition_protein), daily.totalProtein, daily.goal.dailyProteinG, MaterialTheme.colorScheme.primary, compact = false)
                    MacroItem(stringResource(R.string.nutrition_carbs), daily.totalCarbs, daily.goal.dailyCarbsG, MaterialTheme.colorScheme.secondary, compact = false)
                    MacroItem(stringResource(R.string.nutrition_fat), daily.totalFat, daily.goal.dailyFatG, MaterialTheme.colorScheme.error, compact = false)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
                ) {
                    MacroItem(stringResource(R.string.nutrition_protein), daily.totalProtein, daily.goal.dailyProteinG, MaterialTheme.colorScheme.primary, compact = true, modifier = Modifier.weight(1f))
                    MacroItem(stringResource(R.string.nutrition_carbs), daily.totalCarbs, daily.goal.dailyCarbsG, MaterialTheme.colorScheme.secondary, compact = true, modifier = Modifier.weight(1f))
                    MacroItem(stringResource(R.string.nutrition_fat), daily.totalFat, daily.goal.dailyFatG, MaterialTheme.colorScheme.error, compact = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    current: Float,
    goal: Int,
    color: Color,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = nutritionProgress(current, goal)
    val indicator: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(56.dp),
                color = if (progress > 1f) MaterialTheme.colorScheme.error else color,
                strokeWidth = 6.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
    if (compact) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
        ) {
            indicator()
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                stringResource(R.string.nutrition_macro_value, current.roundToInt(), goal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            indicator()
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge)
                Text(
                    stringResource(R.string.nutrition_macro_value, current.roundToInt(), goal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    entries: List<FoodEntry>,
    onDelete: (FoodEntry) -> Unit,
    onEdit: (FoodEntry) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(mealType.localizedName(), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        R.string.nutrition_meal_calories,
                        entries.sumOf { it.calories.toDouble() }.roundToInt()
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.foodName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(
                                R.string.nutrition_entry_detail,
                                entry.amountGrams.roundToInt(),
                                entry.protein.roundToInt(),
                                entry.carbs.roundToInt(),
                                entry.fat.roundToInt()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        stringResource(R.string.nutrition_meal_calories, entry.calories.roundToInt()),
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = { onEdit(entry) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = { onDelete(entry) }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_delete))
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
    var selectedItem by remember(foodItems) { mutableStateOf(foodItems.firstOrNull()) }
    var amount by remember { mutableStateOf("100") }
    var selectedMeal by remember { mutableStateOf(MealType.BREAKFAST) }
    var expanded by remember { mutableStateOf(false) }
    var mealExpanded by remember { mutableStateOf(false) }
    val parsedAmount = parseLocalizedDecimal(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_add_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                if (foodItems.isEmpty()) {
                    Text(
                        stringResource(R.string.nutrition_add_food_first),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedItem?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.nutrition_food)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            foodItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = { selectedItem = item; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.nutrition_amount_grams)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amount.isNotBlank() && parsedAmount?.let(::isValidFoodAmount) != true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = mealExpanded,
                        onExpandedChange = { mealExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMeal.localizedName(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.nutrition_meal)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(mealExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = mealExpanded,
                            onDismissRequest = { mealExpanded = false }
                        ) {
                            MealType.values().forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal.localizedName()) },
                                    onClick = { selectedMeal = meal; mealExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedItem != null && parsedAmount?.let(::isValidFoodAmount) == true,
                onClick = { onConfirm(requireNotNull(selectedItem), requireNotNull(parsedAmount), selectedMeal) }
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun EditEntryDialog(
    entry: FoodEntry,
    onDismiss: () -> Unit,
    onConfirm: (Float, MealType) -> Unit
) {
    var amount by remember(entry.id) { mutableStateOf(entry.amountGrams.toString()) }
    var meal by remember(entry.id) { mutableStateOf(entry.mealType) }
    var mealExpanded by remember { mutableStateOf(false) }
    val parsedAmount = parseLocalizedDecimal(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_edit_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                Text(entry.foodName, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.nutrition_amount_grams)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = parsedAmount?.let(::isValidFoodAmount) != true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = mealExpanded,
                    onExpandedChange = { mealExpanded = it }
                ) {
                    OutlinedTextField(
                        value = meal.localizedName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.nutrition_meal)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(mealExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = mealExpanded,
                        onDismissRequest = { mealExpanded = false }
                    ) {
                        MealType.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.localizedName()) },
                                onClick = { meal = option; mealExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = parsedAmount?.let(::isValidFoodAmount) == true,
                onClick = { onConfirm(requireNotNull(parsedAmount), meal) }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun AddFoodItemDialog(onDismiss: () -> Unit, onConfirm: (FoodItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val parsed = listOf(calories, protein, carbs, fat).map(::parseLocalizedDecimal)
    val item = FoodItem(
        name = name.trim(),
        caloriesPer100g = parsed[0] ?: -1f,
        proteinPer100g = parsed[1] ?: -1f,
        carbsPer100g = parsed[2] ?: -1f,
        fatPer100g = parsed[3] ?: -1f
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_new_food_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.nutrition_food_name)) })
                NutritionNumberField(calories, { calories = it }, R.string.nutrition_calories_kcal)
                NutritionNumberField(protein, { protein = it }, R.string.nutrition_protein_grams)
                NutritionNumberField(carbs, { carbs = it }, R.string.nutrition_carbs_grams)
                NutritionNumberField(fat, { fat = it }, R.string.nutrition_fat_grams)
            }
        },
        confirmButton = {
            Button(enabled = isValidFoodItem(item), onClick = { onConfirm(item) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun GoalDialog(
    current: NutritionGoal,
    onDismiss: () -> Unit,
    onConfirm: (NutritionGoal) -> Unit
) {
    var calories by remember { mutableStateOf(current.dailyCalories.toString()) }
    var protein by remember { mutableStateOf(current.dailyProteinG.toString()) }
    var carbs by remember { mutableStateOf(current.dailyCarbsG.toString()) }
    var fat by remember { mutableStateOf(current.dailyFatG.toString()) }
    val goal = NutritionGoal(
        dailyCalories = calories.toIntOrNull() ?: 0,
        dailyProteinG = protein.toIntOrNull() ?: 0,
        dailyCarbsG = carbs.toIntOrNull() ?: 0,
        dailyFatG = fat.toIntOrNull() ?: 0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_goal_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                NutritionIntegerField(calories, { calories = it }, R.string.nutrition_goal_calories)
                NutritionIntegerField(protein, { protein = it }, R.string.nutrition_goal_protein)
                NutritionIntegerField(carbs, { carbs = it }, R.string.nutrition_goal_carbs)
                NutritionIntegerField(fat, { fat = it }, R.string.nutrition_goal_fat)
            }
        },
        confirmButton = {
            Button(enabled = isValidNutritionGoal(goal), onClick = { onConfirm(goal) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun NutritionNumberField(value: String, onValueChange: (String) -> Unit, label: Int) {
    val parsed = parseLocalizedDecimal(value)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = value.isNotBlank() && (parsed == null || parsed < 0f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NutritionIntegerField(value: String, onValueChange: (String) -> Unit, label: Int) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = value.toIntOrNull()?.let { it <= 0 } != false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NutritionMutationError.userMessage(): String = stringResource(
    when (this) {
        NutritionMutationError.INVALID_AMOUNT -> R.string.nutrition_error_invalid_amount
        NutritionMutationError.INVALID_FOOD -> R.string.nutrition_error_invalid_food
        NutritionMutationError.INVALID_GOAL -> R.string.nutrition_error_invalid_goal
        NutritionMutationError.SAVE_FAILED -> R.string.nutrition_error_save
    }
)

@Composable
private fun MealType.localizedName(): String = stringResource(
    when (this) {
        MealType.BREAKFAST -> R.string.nutrition_meal_breakfast
        MealType.LUNCH -> R.string.nutrition_meal_lunch
        MealType.DINNER -> R.string.nutrition_meal_dinner
        MealType.SNACK -> R.string.nutrition_meal_snack
    }
)
