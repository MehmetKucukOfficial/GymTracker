package com.gymtracker.data.workout

data class CompletedSetForLoad(
    val exerciseName: String,
    val reps: Int,
    val weightKg: Float,
    val completedAt: Long
)
