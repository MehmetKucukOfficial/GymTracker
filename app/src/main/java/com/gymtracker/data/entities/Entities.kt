package com.gymtracker.data.entities

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    indices = [Index("status")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val notes: String = "",
    @ColumnInfo(defaultValue = "'ACTIVE'") val status: WorkoutStatus = WorkoutStatus.ACTIVE,
    val completedAt: Long? = null
)

enum class WorkoutStatus {
    ACTIVE,
    COMPLETED
}

@Entity(
    tableName = "workout_sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean = true,
    val notes: String = "",
    @ColumnInfo(defaultValue = "NULL") val exerciseId: String? = null,
    @ColumnInfo(defaultValue = "NULL") val targetReps: String? = null,
    @ColumnInfo(defaultValue = "0") val exerciseOrder: Int = 0
)

@Entity(
    tableName = "body_measurements",
    indices = [Index("date")]
)
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val weightKg: Float? = null,
    val bodyFatPercent: Float? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val leftArmCm: Float? = null,
    val rightArmCm: Float? = null,
    val leftCalfCm: Float? = null,
    val rightCalfCm: Float? = null,
    val shouldersCm: Float? = null,
    val notes: String = ""
)

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val isCustom: Boolean = true
)

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val foodItemId: Long,
    val foodName: String,
    val amountGrams: Float,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val mealType: MealType = MealType.BREAKFAST
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val photoUri: String,
    val weightKg: Float? = null,
    val notes: String = "",
    val angle: PhotoAngle = PhotoAngle.FRONT,
    @ColumnInfo(defaultValue = "'image/*'") val mimeType: String = "image/*",
    @ColumnInfo(defaultValue = "0") val sizeBytes: Long = 0,
    @ColumnInfo(defaultValue = "0") val storageVersion: Int = 0,
    @ColumnInfo(defaultValue = "NULL") val thumbnailUri: String? = null,
    @ColumnInfo(defaultValue = "0") val widthPx: Int = 0,
    @ColumnInfo(defaultValue = "0") val heightPx: Int = 0
)

enum class PhotoAngle(val displayName: String) {
    FRONT("Ön"),
    BACK("Arka"),
    SIDE_LEFT("Sol Yan"),
    SIDE_RIGHT("Sağ Yan")
}

@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val durationWeeks: Int,
    val daysPerWeek: Int,
    val difficulty: ProgramDifficulty,
    val isPaid: Boolean = false,
    val price: Float = 0f,
    val isPurchased: Boolean = false,
    val coverImageUrl: String = "",
    val creatorName: String = "GymTracker"
)

enum class ProgramDifficulty(val displayName: String) {
    BEGINNER("Başlangıç"),
    INTERMEDIATE("Orta"),
    ADVANCED("İleri")
}

@Entity(
    tableName = "program_workouts",
    foreignKeys = [ForeignKey(
        entity = Program::class,
        parentColumns = ["id"],
        childColumns = ["programId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("programId")]
)
data class ProgramWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val weekNumber: Int,
    val dayNumber: Int,
    val dayName: String,
    /** Kept only as a lossless v6 migration fallback; active reads use ProgramDayExercise. */
    val exercises: String = ""
)

@Entity(
    tableName = "program_day_exercises",
    foreignKeys = [ForeignKey(
        entity = ProgramWorkout::class,
        parentColumns = ["id"],
        childColumns = ["programWorkoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("programWorkoutId"),
        Index(value = ["programWorkoutId", "position"], unique = true),
        Index("exerciseId")
    ]
)
data class ProgramDayExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programWorkoutId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val position: Int,
    val targetSets: Int,
    val targetReps: String,
    val restSeconds: Int
)

@Entity(tableName = "nutrition_goals")
data class NutritionGoal(
    @PrimaryKey val id: Int = 1,
    val dailyCalories: Int = 2500,
    val dailyProteinG: Int = 180,
    val dailyCarbsG: Int = 300,
    val dailyFatG: Int = 80
)

@Entity(tableName = "seed_metadata")
data class SeedMetadata(
    @PrimaryKey val seedKey: String,
    val contentVersion: Int
)
