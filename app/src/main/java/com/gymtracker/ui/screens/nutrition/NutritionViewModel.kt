package com.gymtracker.ui.screens.nutrition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.NutritionDao
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.nutrition.isValidFoodAmount
import com.gymtracker.data.nutrition.isValidFoodItem
import com.gymtracker.data.nutrition.isValidNutritionGoal
import com.gymtracker.data.nutrition.nutritionDayRange
import com.gymtracker.data.nutrition.nutritionEntryTime
import com.gymtracker.data.nutrition.restoredNutritionDate
import com.gymtracker.data.nutrition.updateFoodEntrySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyNutrition(
    val entries: List<FoodEntry> = emptyList(),
    val goal: NutritionGoal = NutritionGoal(),
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f
)

enum class NutritionMutationError {
    INVALID_AMOUNT,
    INVALID_FOOD,
    INVALID_GOAL,
    SAVE_FAILED
}

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val dao: NutritionDao,
    private val savedStateHandle: SavedStateHandle,
    private val clock: Clock
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        restoredNutritionDate(savedStateHandle[SELECTED_DATE_KEY], clock)
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _mutationError = MutableStateFlow<NutritionMutationError?>(null)
    val mutationError: StateFlow<NutritionMutationError?> = _mutationError.asStateFlow()

    private val _deletedEntry = MutableStateFlow<FoodEntry?>(null)
    val deletedEntry: StateFlow<FoodEntry?> = _deletedEntry.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyNutrition: StateFlow<DailyNutrition> = combine(
        _selectedDate.flatMapLatest { date ->
            val range = nutritionDayRange(date, clock.zone)
            dao.getEntriesForDay(range.startInclusiveMillis, range.endExclusiveMillis)
        },
        dao.getGoal().map { it ?: NutritionGoal() }
    ) { entries, goal ->
        DailyNutrition(
            entries = entries,
            goal = goal,
            totalCalories = entries.sumOf { it.calories.toDouble() }.toFloat(),
            totalProtein = entries.sumOf { it.protein.toDouble() }.toFloat(),
            totalCarbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
            totalFat = entries.sumOf { it.fat.toDouble() }.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyNutrition())

    val foodItems = dao.getAllFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPreviousDay() = selectDate(_selectedDate.value.minusDays(1))

    fun selectNextDay() {
        val next = _selectedDate.value.plusDays(1)
        if (!next.isAfter(LocalDate.now(clock))) selectDate(next)
    }

    fun selectToday() = selectDate(LocalDate.now(clock))

    fun addEntry(
        foodItem: FoodItem,
        amountGrams: Float,
        mealType: MealType,
        onSaved: () -> Unit = {}
    ) {
        if (!isValidFoodAmount(amountGrams)) {
            _mutationError.value = NutritionMutationError.INVALID_AMOUNT
            return
        }
        if (!isValidFoodItem(foodItem)) {
            _mutationError.value = NutritionMutationError.INVALID_FOOD
            return
        }
        val factor = amountGrams / 100f
        viewModelScope.launch {
            runCatching {
                dao.insertEntry(
                    FoodEntry(
                        date = nutritionEntryTime(_selectedDate.value, clock),
                        foodItemId = foodItem.id,
                        foodName = foodItem.name,
                        amountGrams = amountGrams,
                        calories = foodItem.caloriesPer100g * factor,
                        protein = foodItem.proteinPer100g * factor,
                        carbs = foodItem.carbsPer100g * factor,
                        fat = foodItem.fatPer100g * factor,
                        mealType = mealType
                    )
                )
            }.onSuccess { onSaved() }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun addCustomFood(item: FoodItem, onSaved: () -> Unit = {}) {
        if (!isValidFoodItem(item)) {
            _mutationError.value = NutritionMutationError.INVALID_FOOD
            return
        }
        viewModelScope.launch {
            runCatching { dao.insertFoodItem(item) }
                .onSuccess { onSaved() }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            runCatching { dao.deleteEntry(entry) }
                .onSuccess { _deletedEntry.value = entry }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun updateEntry(
        entry: FoodEntry,
        amountGrams: Float,
        mealType: MealType,
        onSaved: () -> Unit = {}
    ) {
        val updated = updateFoodEntrySnapshot(entry, amountGrams)?.copy(mealType = mealType)
        if (updated == null) {
            _mutationError.value = NutritionMutationError.INVALID_AMOUNT
            return
        }
        viewModelScope.launch {
            runCatching { dao.updateEntry(updated) }
                .onSuccess { onSaved() }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun undoDelete(entry: FoodEntry) {
        if (_deletedEntry.value?.id != entry.id) return
        viewModelScope.launch {
            runCatching { dao.insertEntry(entry) }
                .onSuccess { _deletedEntry.value = null }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun consumeDeletedEntry(entry: FoodEntry) {
        if (_deletedEntry.value?.id == entry.id) _deletedEntry.value = null
    }

    fun updateGoal(goal: NutritionGoal, onSaved: () -> Unit = {}) {
        if (!isValidNutritionGoal(goal)) {
            _mutationError.value = NutritionMutationError.INVALID_GOAL
            return
        }
        viewModelScope.launch {
            runCatching { dao.upsertGoal(goal) }
                .onSuccess { onSaved() }
                .onFailure { _mutationError.value = NutritionMutationError.SAVE_FAILED }
        }
    }

    fun clearMutationError() {
        _mutationError.value = null
    }

    private fun selectDate(date: LocalDate) {
        savedStateHandle[SELECTED_DATE_KEY] = date.toString()
        _selectedDate.value = date
    }

    private companion object {
        const val SELECTED_DATE_KEY = "nutrition_selected_date"
    }
}
