package com.gymtracker.domain

import org.json.JSONArray
import org.json.JSONObject

data class ProgramExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int = 90
)

object ProgramExerciseSerializer {
    fun toJson(exercises: List<ProgramExercise>): String {
        val array = JSONArray()
        exercises.forEach { exercise ->
            val obj = JSONObject()
            obj.put("name", exercise.name)
            obj.put("sets", exercise.sets)
            obj.put("reps", exercise.reps)
            obj.put("rest", exercise.restSeconds)
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(json: String): List<ProgramExercise> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            ProgramExercise(
                name = obj.getString("name"),
                sets = obj.getInt("sets"),
                reps = obj.getString("reps"),
                restSeconds = obj.optInt("rest", 90)
            )
        }
    }
}
