package com.gymtracker.data.programs

import androidx.room.withTransaction
import com.gymtracker.data.dao.ProgramDao
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.domain.ProgramExercise
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramSeedInitializer @Inject constructor(
    private val database: AppDatabase,
    private val programDao: ProgramDao
) {

    suspend fun seedIfNeeded() = database.withTransaction {
        val claimed = programDao.insertSeedMetadata(SeedMetadata(SEED_KEY, CONTENT_VERSION))
        if (claimed == -1L) return@withTransaction

        // Existing v3 installations may already contain the legacy default data.
        // Mark them as handled instead of creating duplicate programs.
        if (programDao.getProgramCount() > 0) return@withTransaction

        defaults.forEach { definition ->
            val programId = programDao.insertProgram(definition.program)
            definition.days.forEach { day ->
                val workoutId = programDao.insertWorkout(day.toEntity(programId))
                programDao.insertDayExercises(day.toExerciseEntities(workoutId))
            }
        }
    }

    private data class ProgramDefinition(
        val program: Program,
        val days: List<DayDefinition>
    )

    private data class DayDefinition(
        val number: Int,
        val name: String,
        val exercises: List<ProgramExercise>
    ) {
        fun toEntity(programId: Long) = ProgramWorkout(
            programId = programId,
            weekNumber = 1,
            dayNumber = number,
            dayName = name,
            exercises = ""
        )

        fun toExerciseEntities(workoutId: Long) = exercises.mapIndexed { position, exercise ->
            ProgramDayExercise(
                programWorkoutId = workoutId,
                exerciseId = exercise.exerciseId,
                exerciseName = exercise.name,
                position = position,
                targetSets = exercise.sets,
                targetReps = exercise.reps,
                restSeconds = exercise.restSeconds
            )
        }
    }

    private fun exercise(name: String, sets: Int, reps: String, rest: Int = 90) =
        ProgramExercise(name, sets, reps, rest)

    private fun day(number: Int, name: String, vararg exercises: ProgramExercise) =
        DayDefinition(number, name, exercises.toList())

    private val defaults = listOf(
        ProgramDefinition(
            Program(
                name = "Push Pull Legs (PPL)",
                description = "Klasik 6 günlük bölünmüş antrenman. İtme, çekme ve bacak günleri ile dengeli kas gelişimi.",
                durationWeeks = 12,
                daysPerWeek = 6,
                difficulty = ProgramDifficulty.INTERMEDIATE
            ),
            listOf(
                day(1, "İtme (Göğüs/Omuz/Triceps) A", exercise("Bench Press", 4, "8-10", 120), exercise("İnkline Dumbbell Press", 3, "10-12"), exercise("Pec Deck", 3, "12-15", 60), exercise("Overhead Press", 3, "8-10"), exercise("Lateral Raise", 4, "12-15", 60), exercise("Triceps Pushdown", 3, "12-15", 60)),
                day(2, "Çekme (Sırt/Biceps) A", exercise("Deadlift", 3, "5", 150), exercise("Pull-up", 4, "8-10"), exercise("Barbell Row", 4, "8-10"), exercise("Lat Pulldown", 3, "10-12", 60), exercise("Face Pull", 3, "15-20", 60), exercise("Barbell Curl", 3, "10-12", 60)),
                day(3, "Bacak A", exercise("Squat", 4, "6-8", 150), exercise("Romanian Deadlift", 3, "10-12"), exercise("Leg Press", 3, "10-12"), exercise("Leg Curl", 3, "12-15", 60), exercise("Calf Raise", 4, "15-20", 45)),
                day(4, "İtme (Göğüs/Omuz/Triceps) B", exercise("Decline Bench Press", 4, "8-10", 120), exercise("Arnold Press", 3, "10-12"), exercise("Cable Crossover", 3, "12-15", 60), exercise("Skull Crusher", 3, "10-12", 60), exercise("Cable Lateral Raise", 3, "15", 45)),
                day(5, "Çekme (Sırt/Biceps) B", exercise("T-Bar Row", 4, "8-10"), exercise("Chin-up", 3, "8-10"), exercise("Seated Cable Row", 3, "10-12", 60), exercise("Straight-Arm Pulldown", 3, "12-15", 60), exercise("Hammer Curl", 3, "10-12", 60)),
                day(6, "Bacak B", exercise("Front Squat", 4, "8", 120), exercise("Leg Extension", 3, "12-15", 60), exercise("Bulgarian Split Squat", 3, "10"), exercise("Hip Thrust", 3, "10-12"), exercise("Seated Calf Raise", 4, "15-20", 45))
            )
        ),
        ProgramDefinition(
            Program(name = "Full Body 3 Gün", description = "Başlangıç seviyesi için hafta 3 gün tam vücut antrenmanı.", durationWeeks = 8, daysPerWeek = 3, difficulty = ProgramDifficulty.BEGINNER),
            listOf(
                day(1, "Tam Vücut A", exercise("Squat", 3, "8-10", 120), exercise("Bench Press", 3, "8-10"), exercise("Barbell Row", 3, "8-10"), exercise("Plank", 3, "30 sn", 60)),
                day(2, "Tam Vücut B", exercise("Deadlift", 3, "6-8", 150), exercise("Overhead Press", 3, "8-10"), exercise("Lat Pulldown", 3, "10-12", 60), exercise("Crunch", 3, "15", 45)),
                day(3, "Tam Vücut C", exercise("Leg Press", 3, "10-12"), exercise("Dumbbell Bench Press", 3, "10-12"), exercise("Seated Cable Row", 3, "10-12", 60), exercise("Russian Twist", 3, "20", 45))
            )
        ),
        ProgramDefinition(
            Program(name = "Hypertrofi Pro", description = "Maksimum kas kütlesi için bilimsel temelli 16 haftalık program.", durationWeeks = 16, daysPerWeek = 5, difficulty = ProgramDifficulty.ADVANCED, isPaid = true, price = 149.99f, creatorName = "Coach Mehmet"),
            listOf(
                day(1, "Göğüs", exercise("Bench Press", 5, "6-8", 150), exercise("İnkline Bench Press", 4, "8-10", 120), exercise("Cable Crossover", 3, "12-15", 60), exercise("Pec Deck", 3, "15", 60), exercise("Dips", 3, "10-12")),
                day(2, "Sırt", exercise("Deadlift", 4, "5", 180), exercise("Pull-up", 4, "8-10"), exercise("Barbell Row", 4, "8-10"), exercise("Seated Cable Row", 3, "10-12", 60), exercise("Straight-Arm Pulldown", 3, "12-15", 60)),
                day(3, "Omuz", exercise("Overhead Press", 4, "6-8", 120), exercise("Arnold Press", 3, "10-12"), exercise("Lateral Raise", 4, "12-15", 60), exercise("Rear Delt Fly", 3, "12-15", 60), exercise("Face Pull", 3, "15-20", 60)),
                day(4, "Kol", exercise("Barbell Curl", 4, "8-10"), exercise("Hammer Curl", 3, "10-12", 60), exercise("Triceps Pushdown", 4, "10-12"), exercise("Skull Crusher", 3, "10-12", 60), exercise("Cable Curl", 3, "12-15", 60)),
                day(5, "Bacak", exercise("Squat", 5, "5-8", 180), exercise("Romanian Deadlift", 4, "8-10", 120), exercise("Leg Press", 3, "10-12"), exercise("Leg Curl", 3, "12-15", 60), exercise("Calf Raise", 4, "15-20", 45))
            )
        ),
        ProgramDefinition(
            Program(name = "Athletic Performance", description = "Güç ve çevikliği birleştiren sporcular için 12 haftalık program.", durationWeeks = 12, daysPerWeek = 5, difficulty = ProgramDifficulty.ADVANCED, isPaid = true, price = 199.99f, creatorName = "Coach Ali"),
            listOf(
                day(1, "Patlayıcı Bacak", exercise("Squat", 5, "5", 180), exercise("Front Squat", 3, "6-8", 120), exercise("Hip Thrust", 3, "8-10"), exercise("Calf Raise", 4, "12-15", 45), exercise("Hanging Leg Raise", 3, "12-15", 60)),
                day(2, "Üst Vücut Güç", exercise("Bench Press", 5, "5", 150), exercise("Overhead Press", 4, "6-8", 120), exercise("Dips", 3, "10"), exercise("Push-up", 3, "15-20", 60), exercise("Plank", 3, "45 sn", 60)),
                day(3, "Çekme & Sırt", exercise("Deadlift", 4, "5", 180), exercise("Pull-up", 4, "8-10"), exercise("Barbell Row", 4, "8"), exercise("Face Pull", 3, "15-20", 60)),
                day(4, "Tek Bacak & Hamstring", exercise("Bulgarian Split Squat", 3, "10"), exercise("Romanian Deadlift", 4, "8-10", 120), exercise("Leg Curl", 3, "12-15", 60), exercise("Step-up", 3, "10", 60)),
                day(5, "Çeviklik & Kondisyon", exercise("Lateral Raise", 3, "15", 45), exercise("Cable Curl", 3, "12-15", 60), exercise("Triceps Pushdown", 3, "12-15", 60), exercise("Russian Twist", 3, "20", 45), exercise("Mountain Climber", 3, "30 sn", 45))
            )
        ),
        ProgramDefinition(
            Program(name = "Cut & Lean", description = "Yağ yakımı için optimize edilmiş 8 haftalık program. HIIT ve ağırlık kombinasyonu.", durationWeeks = 8, daysPerWeek = 4, difficulty = ProgramDifficulty.INTERMEDIATE, isPaid = true, price = 99.99f, creatorName = "Coach Sara"),
            listOf(
                day(1, "Üst Vücut İtme", exercise("Bench Press", 4, "8-10"), exercise("Overhead Press", 3, "8-10"), exercise("Triceps Pushdown", 3, "12-15", 45), exercise("Cable Crossover", 3, "12-15", 45)),
                day(2, "Alt Vücut", exercise("Squat", 4, "8-10", 120), exercise("Leg Press", 3, "10-12"), exercise("Leg Curl", 3, "12-15", 60), exercise("Calf Raise", 4, "15-20", 45)),
                day(3, "Üst Vücut Çekme", exercise("Pull-up", 4, "8-10"), exercise("Barbell Row", 3, "8-10"), exercise("Face Pull", 3, "15-20", 45), exercise("Barbell Curl", 3, "10-12", 45)),
                day(4, "Tam Vücut Kondisyon", exercise("Deadlift", 3, "6-8", 120), exercise("Mountain Climber", 3, "30 sn", 45), exercise("Russian Twist", 3, "20", 45), exercise("Plank", 3, "45 sn", 45))
            )
        )
    )

    private companion object {
        const val SEED_KEY = "default-programs-v1"
        const val CONTENT_VERSION = 1
    }
}
