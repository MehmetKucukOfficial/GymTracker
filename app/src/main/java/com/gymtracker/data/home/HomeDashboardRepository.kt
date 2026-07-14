package com.gymtracker.data.home

import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.dao.NutritionDao
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.entities.WorkoutStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class HomeDashboardData(
    val totalWorkouts: Int = 0,
    val weekWorkouts: Int = 0,
    val latestWeight: Float? = null,
    val todayCalories: Int = 0,
    val todayNutritionEntryCount: Int = 0,
    val activeWorkoutId: Long? = null,
    val activeWorkoutName: String? = null,
    val lastWorkoutName: String? = null,
    val lastWorkoutDate: Long? = null
)

@Singleton
class HomeDashboardRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val measurementDao: MeasurementDao,
    private val nutritionDao: NutritionDao
) {
    fun observeDashboard(
        weekStartMillis: Long,
        dayStartMillis: Long,
        nextDayStartMillis: Long
    ): Flow<HomeDashboardData> = combine(
        workoutDao.getAllSessions(),
        measurementDao.getAll(),
        nutritionDao.getEntriesForDay(dayStartMillis, nextDayStartMillis)
    ) { sessions, measurements, entries ->
        val completedSessions = sessions.filter { it.status == WorkoutStatus.COMPLETED }
        val activeSession = sessions.firstOrNull { it.status == WorkoutStatus.ACTIVE }
        HomeDashboardData(
            totalWorkouts = completedSessions.size,
            weekWorkouts = completedSessions.count { it.date >= weekStartMillis },
            latestWeight = measurements.firstOrNull()?.weightKg,
            todayCalories = entries.sumOf { it.calories.toDouble() }.toInt(),
            todayNutritionEntryCount = entries.size,
            activeWorkoutId = activeSession?.id,
            activeWorkoutName = activeSession?.name,
            lastWorkoutName = completedSessions.firstOrNull()?.name,
            lastWorkoutDate = completedSessions.firstOrNull()?.date
        )
    }
}
