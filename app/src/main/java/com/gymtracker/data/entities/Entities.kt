package com.gymtracker.data.entities

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val notes: String = ""
)

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
    val notes: String = ""
)

@Entity(tableName = "body_measurements")
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

enum class MealType(val displayName: String) {
    BREAKFAST("Kahvaltı"),
    LUNCH("Öğle"),
    DINNER("Akşam"),
    SNACK("Ara Öğün")
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
    @ColumnInfo(defaultValue = "0") val storageVersion: Int = 0
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
    val exercises: String  // JSON serialized list of exercises
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
