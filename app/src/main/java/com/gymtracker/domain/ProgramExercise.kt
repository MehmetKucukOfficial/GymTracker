package com.gymtracker.domain

import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale

data class ProgramExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int = 90,
    val exerciseId: String = ExerciseIdentity.fromLegacyName(name)
)

/**
 * Freezes the identifiers used when the v6 name-based catalog is normalized.
 * Persisted IDs must remain unchanged when a display name is translated later.
 */
object ExerciseIdentity {
    fun fromLegacyName(name: String): String {
        val turkishSafe = name.trim()
            .replace('ı', 'i')
            .replace('İ', 'I')
            .replace('ş', 's')
            .replace('Ş', 'S')
            .replace('ğ', 'g')
            .replace('Ğ', 'G')
            .replace('ü', 'u')
            .replace('Ü', 'U')
            .replace('ö', 'o')
            .replace('Ö', 'O')
            .replace('ç', 'c')
            .replace('Ç', 'C')
        val slug = Normalizer.normalize(turkishSafe, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        require(slug.isNotEmpty()) { "Exercise name must produce a stable identifier" }
        return "exercise.$slug"
    }
}

object ProgramExerciseSerializer {
    fun toJson(exercises: List<ProgramExercise>): String {
        val array = JSONArray()
        exercises.forEach { exercise ->
            val obj = JSONObject()
            obj.put("name", exercise.name)
            obj.put("sets", exercise.sets)
            obj.put("reps", exercise.reps)
            obj.put("rest", exercise.restSeconds)
            obj.put("exerciseId", exercise.exerciseId)
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
                restSeconds = obj.optInt("rest", 90),
                exerciseId = obj.optString("exerciseId")
                    .takeIf { it.isNotBlank() }
                    ?: ExerciseIdentity.fromLegacyName(obj.getString("name"))
            )
        }
    }
}
