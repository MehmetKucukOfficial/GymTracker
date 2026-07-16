package com.gymtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseIdentityTest {
    @Test
    fun legacyNames_produceLocaleIndependentStableIds() {
        assertEquals("exercise.bench-press", ExerciseIdentity.fromLegacyName("Bench Press"))
        assertEquals(
            "exercise.inkline-dumbbell-press",
            ExerciseIdentity.fromLegacyName("İnkline Dumbbell Press")
        )
    }

    @Test
    fun exerciseLibrary_hasUniqueStableIds() {
        val ids = ExerciseLibrary.exercises.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.all { it.startsWith("exercise.") })
    }
}
