package com.gymtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.dao.NutritionDao
import com.gymtracker.data.dao.WorkoutDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeStats(
    val totalWorkouts: Int = 0,
    val weekWorkouts: Int = 0,
    val latestWeight: Float? = null,
    val todayCalories: Int = 0,
    val lastWorkoutName: String? = null,
    val lastWorkoutDate: Long? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val measurementDao: MeasurementDao,
    private val nutritionDao: NutritionDao
) : ViewModel() {

    private val _stats = MutableStateFlow(HomeStats())
    val stats = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            workoutDao.getAllSessions().collect { sessions ->
                val weekSessions = sessions.filter { it.date >= weekAgo }
                val latestMeasurement = measurementDao.getLatest()
                val todayEntries = nutritionDao.getEntriesForDay(
                    todayStart, todayStart + 86_400_000L
                ).first()
                val todayCalories = todayEntries.sumOf { it.calories.toDouble() }.toInt()

                _stats.value = HomeStats(
                    totalWorkouts = sessions.size,
                    weekWorkouts = weekSessions.size,
                    latestWeight = latestMeasurement?.weightKg,
                    todayCalories = todayCalories,
                    lastWorkoutName = sessions.firstOrNull()?.name,
                    lastWorkoutDate = sessions.firstOrNull()?.date
                )
            }
        }
    }
}
