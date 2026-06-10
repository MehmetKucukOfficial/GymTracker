package com.gymtracker.ui.screens.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.ProgramDao
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.domain.ProgramExercise
import com.gymtracker.domain.ProgramExerciseSerializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val dao: ProgramDao
) : ViewModel() {

    val programs = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedDefaultPrograms()
    }

    fun getWorkoutsForProgram(programId: Long) = dao.getWorkoutsForProgram(programId)

    fun purchaseProgram(program: Program) {
        viewModelScope.launch {
            dao.updateProgram(program.copy(isPurchased = true))
        }
    }

    private fun pe(name: String, sets: Int, reps: String, rest: Int = 90) =
        ProgramExercise(name, sets, reps, rest)

    private fun day(programId: Long, week: Int, dayNumber: Int, dayName: String, exercises: List<ProgramExercise>) =
        ProgramWorkout(
            programId = programId,
            weekNumber = week,
            dayNumber = dayNumber,
            dayName = dayName,
            exercises = ProgramExerciseSerializer.toJson(exercises)
        )

    private fun seedDefaultPrograms() {
        viewModelScope.launch {
            val existing = programs.value
            if (existing.isNotEmpty()) return@launch

            // Ücretsiz programlar
            val pplId = dao.insertProgram(Program(
                name = "Push Pull Legs (PPL)",
                description = "Klasik 6 günlük bölünmüş antrenman. İtme, çekme ve bacak günleri ile dengeli kas gelişimi.",
                durationWeeks = 12,
                daysPerWeek = 6,
                difficulty = ProgramDifficulty.INTERMEDIATE,
                isPaid = false
            ))
            dao.insertWorkouts(listOf(
                day(pplId, 1, 1, "İtme (Göğüs/Omuz/Triceps) A", listOf(
                    pe("Bench Press", 4, "8-10", 120),
                    pe("İnkline Dumbbell Press", 3, "10-12", 90),
                    pe("Pec Deck", 3, "12-15", 60),
                    pe("Overhead Press", 3, "8-10", 90),
                    pe("Lateral Raise", 4, "12-15", 60),
                    pe("Triceps Pushdown", 3, "12-15", 60)
                )),
                day(pplId, 1, 2, "Çekme (Sırt/Biceps) A", listOf(
                    pe("Deadlift", 3, "5", 150),
                    pe("Pull-up", 4, "8-10", 90),
                    pe("Barbell Row", 4, "8-10", 90),
                    pe("Lat Pulldown", 3, "10-12", 60),
                    pe("Face Pull", 3, "15-20", 60),
                    pe("Barbell Curl", 3, "10-12", 60)
                )),
                day(pplId, 1, 3, "Bacak A", listOf(
                    pe("Squat", 4, "6-8", 150),
                    pe("Romanian Deadlift", 3, "10-12", 90),
                    pe("Leg Press", 3, "10-12", 90),
                    pe("Leg Curl", 3, "12-15", 60),
                    pe("Calf Raise", 4, "15-20", 45)
                )),
                day(pplId, 1, 4, "İtme (Göğüs/Omuz/Triceps) B", listOf(
                    pe("Decline Bench Press", 4, "8-10", 120),
                    pe("Arnold Press", 3, "10-12", 90),
                    pe("Cable Crossover", 3, "12-15", 60),
                    pe("Skull Crusher", 3, "10-12", 60),
                    pe("Cable Lateral Raise", 3, "15", 45)
                )),
                day(pplId, 1, 5, "Çekme (Sırt/Biceps) B", listOf(
                    pe("T-Bar Row", 4, "8-10", 90),
                    pe("Chin-up", 3, "8-10", 90),
                    pe("Seated Cable Row", 3, "10-12", 60),
                    pe("Straight-Arm Pulldown", 3, "12-15", 60),
                    pe("Hammer Curl", 3, "10-12", 60)
                )),
                day(pplId, 1, 6, "Bacak B", listOf(
                    pe("Front Squat", 4, "8", 120),
                    pe("Leg Extension", 3, "12-15", 60),
                    pe("Bulgarian Split Squat", 3, "10", 90),
                    pe("Hip Thrust", 3, "10-12", 90),
                    pe("Seated Calf Raise", 4, "15-20", 45)
                ))
            ))

            val fullBodyId = dao.insertProgram(Program(
                name = "Full Body 3 Gün",
                description = "Başlangıç seviyesi için hafta 3 gün tam vücut antrenmanı.",
                durationWeeks = 8,
                daysPerWeek = 3,
                difficulty = ProgramDifficulty.BEGINNER,
                isPaid = false
            ))
            dao.insertWorkouts(listOf(
                day(fullBodyId, 1, 1, "Tam Vücut A", listOf(
                    pe("Squat", 3, "8-10", 120),
                    pe("Bench Press", 3, "8-10", 90),
                    pe("Barbell Row", 3, "8-10", 90),
                    pe("Plank", 3, "30 sn", 60)
                )),
                day(fullBodyId, 1, 2, "Tam Vücut B", listOf(
                    pe("Deadlift", 3, "6-8", 150),
                    pe("Overhead Press", 3, "8-10", 90),
                    pe("Lat Pulldown", 3, "10-12", 60),
                    pe("Crunch", 3, "15", 45)
                )),
                day(fullBodyId, 1, 3, "Tam Vücut C", listOf(
                    pe("Leg Press", 3, "10-12", 90),
                    pe("Dumbbell Bench Press", 3, "10-12", 90),
                    pe("Seated Cable Row", 3, "10-12", 60),
                    pe("Russian Twist", 3, "20", 45)
                ))
            ))

            // Ücretli programlar
            val hypertrofiId = dao.insertProgram(Program(
                name = "Hypertrofi Pro",
                description = "Maksimum kas kütlesi için bilimsel temelli 16 haftalık programa. Periodizasyon, deload haftaları ve detaylı beslenme rehberi dahil.",
                durationWeeks = 16,
                daysPerWeek = 5,
                difficulty = ProgramDifficulty.ADVANCED,
                isPaid = true,
                price = 149.99f,
                isPurchased = false,
                creatorName = "Coach Mehmet"
            ))
            dao.insertWorkouts(listOf(
                day(hypertrofiId, 1, 1, "Göğüs", listOf(
                    pe("Bench Press", 5, "6-8", 150),
                    pe("İnkline Bench Press", 4, "8-10", 120),
                    pe("Cable Crossover", 3, "12-15", 60),
                    pe("Pec Deck", 3, "15", 60),
                    pe("Dips", 3, "10-12", 90)
                )),
                day(hypertrofiId, 1, 2, "Sırt", listOf(
                    pe("Deadlift", 4, "5", 180),
                    pe("Pull-up", 4, "8-10", 90),
                    pe("Barbell Row", 4, "8-10", 90),
                    pe("Seated Cable Row", 3, "10-12", 60),
                    pe("Straight-Arm Pulldown", 3, "12-15", 60)
                )),
                day(hypertrofiId, 1, 3, "Omuz", listOf(
                    pe("Overhead Press", 4, "6-8", 120),
                    pe("Arnold Press", 3, "10-12", 90),
                    pe("Lateral Raise", 4, "12-15", 60),
                    pe("Rear Delt Fly", 3, "12-15", 60),
                    pe("Face Pull", 3, "15-20", 60)
                )),
                day(hypertrofiId, 1, 4, "Kol", listOf(
                    pe("Barbell Curl", 4, "8-10", 90),
                    pe("Hammer Curl", 3, "10-12", 60),
                    pe("Triceps Pushdown", 4, "10-12", 60),
                    pe("Skull Crusher", 3, "10-12", 60),
                    pe("Cable Curl", 3, "12-15", 60)
                )),
                day(hypertrofiId, 1, 5, "Bacak", listOf(
                    pe("Squat", 5, "5-8", 180),
                    pe("Romanian Deadlift", 4, "8-10", 120),
                    pe("Leg Press", 3, "10-12", 90),
                    pe("Leg Curl", 3, "12-15", 60),
                    pe("Calf Raise", 4, "15-20", 45)
                ))
            ))

            val athleticId = dao.insertProgram(Program(
                name = "Athletic Performance",
                description = "Güç ve çevikliği birleştiren sporcular için 12 haftalık program. Olimpik hareketler ve patlayıcı güç çalışmaları.",
                durationWeeks = 12,
                daysPerWeek = 5,
                difficulty = ProgramDifficulty.ADVANCED,
                isPaid = true,
                price = 199.99f,
                isPurchased = false,
                creatorName = "Coach Ali"
            ))
            dao.insertWorkouts(listOf(
                day(athleticId, 1, 1, "Patlayıcı Bacak", listOf(
                    pe("Squat", 5, "5", 180),
                    pe("Front Squat", 3, "6-8", 120),
                    pe("Hip Thrust", 3, "8-10", 90),
                    pe("Calf Raise", 4, "12-15", 45),
                    pe("Hanging Leg Raise", 3, "12-15", 60)
                )),
                day(athleticId, 1, 2, "Üst Vücut Güç", listOf(
                    pe("Bench Press", 5, "5", 150),
                    pe("Overhead Press", 4, "6-8", 120),
                    pe("Dips", 3, "10", 90),
                    pe("Push-up", 3, "15-20", 60),
                    pe("Plank", 3, "45 sn", 60)
                )),
                day(athleticId, 1, 3, "Çekme & Sırt", listOf(
                    pe("Deadlift", 4, "5", 180),
                    pe("Pull-up", 4, "8-10", 90),
                    pe("Barbell Row", 4, "8", 90),
                    pe("Face Pull", 3, "15-20", 60)
                )),
                day(athleticId, 1, 4, "Tek Bacak & Hamstring", listOf(
                    pe("Bulgarian Split Squat", 3, "10", 90),
                    pe("Romanian Deadlift", 4, "8-10", 120),
                    pe("Leg Curl", 3, "12-15", 60),
                    pe("Step-up", 3, "10", 60)
                )),
                day(athleticId, 1, 5, "Çeviklik & Kondisyon", listOf(
                    pe("Lateral Raise", 3, "15", 45),
                    pe("Cable Curl", 3, "12-15", 60),
                    pe("Triceps Pushdown", 3, "12-15", 60),
                    pe("Russian Twist", 3, "20", 45),
                    pe("Mountain Climber", 3, "30 sn", 45)
                ))
            ))

            val cutLeanId = dao.insertProgram(Program(
                name = "Cut & Lean",
                description = "Yağ yakımı için optimize edilmiş 8 haftalık program. HIIT + ağırlık kombinasyonu.",
                durationWeeks = 8,
                daysPerWeek = 4,
                difficulty = ProgramDifficulty.INTERMEDIATE,
                isPaid = true,
                price = 99.99f,
                isPurchased = false,
                creatorName = "Coach Sara"
            ))
            dao.insertWorkouts(listOf(
                day(cutLeanId, 1, 1, "Üst Vücut İtme", listOf(
                    pe("Bench Press", 4, "8-10", 90),
                    pe("Overhead Press", 3, "8-10", 90),
                    pe("Triceps Pushdown", 3, "12-15", 45),
                    pe("Cable Crossover", 3, "12-15", 45)
                )),
                day(cutLeanId, 1, 2, "Alt Vücut", listOf(
                    pe("Squat", 4, "8-10", 120),
                    pe("Leg Press", 3, "10-12", 90),
                    pe("Leg Curl", 3, "12-15", 60),
                    pe("Calf Raise", 4, "15-20", 45)
                )),
                day(cutLeanId, 1, 3, "Üst Vücut Çekme", listOf(
                    pe("Pull-up", 4, "8-10", 90),
                    pe("Barbell Row", 3, "8-10", 90),
                    pe("Face Pull", 3, "15-20", 45),
                    pe("Barbell Curl", 3, "10-12", 45)
                )),
                day(cutLeanId, 1, 4, "Tam Vücut Kondisyon", listOf(
                    pe("Deadlift", 3, "6-8", 120),
                    pe("Mountain Climber", 3, "30 sn", 45),
                    pe("Russian Twist", 3, "20", 45),
                    pe("Plank", 3, "45 sn", 45)
                ))
            ))
        }
    }
}
