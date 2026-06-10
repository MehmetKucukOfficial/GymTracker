package com.gymtracker.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.NutritionDao
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DailyNutrition(
    val entries: List<FoodEntry> = emptyList(),
    val goal: NutritionGoal = NutritionGoal(),
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val dao: NutritionDao
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(todayStart())
    val selectedDate = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyNutrition: StateFlow<DailyNutrition> = combine(
        _selectedDate.flatMapLatest { start ->
            dao.getEntriesForDay(start, start + 86_400_000L)
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyNutrition())

    val foodItems = dao.getAllFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(foodItem: FoodItem, amountGrams: Float, mealType: MealType) {
        val factor = amountGrams / 100f
        viewModelScope.launch {
            dao.insertEntry(FoodEntry(
                date = _selectedDate.value,
                foodItemId = foodItem.id,
                foodName = foodItem.name,
                amountGrams = amountGrams,
                calories = foodItem.caloriesPer100g * factor,
                protein = foodItem.proteinPer100g * factor,
                carbs = foodItem.carbsPer100g * factor,
                fat = foodItem.fatPer100g * factor,
                mealType = mealType
            ))
        }
    }

    fun addCustomFood(item: FoodItem) {
        viewModelScope.launch { dao.insertFoodItem(item) }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch { dao.deleteEntry(entry) }
    }

    fun updateGoal(goal: NutritionGoal) {
        viewModelScope.launch { dao.upsertGoal(goal) }
    }

    private fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
