package com.gymtracker.data.nutrition

import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.NutritionGoal
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class NutritionDayRange(
    val startInclusiveMillis: Long,
    val endExclusiveMillis: Long
)

fun nutritionDayRange(date: LocalDate, zoneId: ZoneId): NutritionDayRange = NutritionDayRange(
    startInclusiveMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
    endExclusiveMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
)

fun nutritionEntryTime(date: LocalDate, clock: Clock): Long {
    val today = LocalDate.now(clock)
    if (date == today) return clock.instant().toEpochMilli()
    return date.atTime(LocalTime.now(clock)).atZone(clock.zone).toInstant().toEpochMilli()
}

fun nutritionProgress(current: Float, goal: Number): Float {
    val target = goal.toFloat()
    if (!current.isFinite() || !target.isFinite() || target <= 0f) return 0f
    return (current / target).coerceAtLeast(0f)
}

fun isValidNutritionGoal(goal: NutritionGoal): Boolean =
    goal.dailyCalories > 0 &&
        goal.dailyProteinG > 0 &&
        goal.dailyCarbsG > 0 &&
        goal.dailyFatG > 0

fun isValidFoodAmount(amountGrams: Float): Boolean = amountGrams.isFinite() && amountGrams > 0f

fun isValidFoodItem(item: FoodItem): Boolean =
    item.name.isNotBlank() &&
        listOf(
            item.caloriesPer100g,
            item.proteinPer100g,
            item.carbsPer100g,
            item.fatPer100g
        ).all { it.isFinite() && it >= 0f }
