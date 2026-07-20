package com.gymtracker.data.portability

import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import kotlinx.serialization.Serializable

const val PORTABLE_ARCHIVE_FORMAT = "gymtracker-export"
const val PORTABLE_ARCHIVE_VERSION = 1
const val PORTABLE_DATABASE_SCHEMA_VERSION = 9
const val PORTABLE_DATA_ENTRY = "data.json"

@Serializable
data class PortableArchive(
    val format: String = PORTABLE_ARCHIVE_FORMAT,
    val formatVersion: Int = PORTABLE_ARCHIVE_VERSION,
    val databaseSchemaVersion: Int = PORTABLE_DATABASE_SCHEMA_VERSION,
    val exportedAtEpochMillis: Long,
    val appVersionName: String,
    val settings: PortableSettings,
    val data: PortableData
)

@Serializable
data class PortableSettings(
    val themeMode: String
)

@Serializable
data class PortableData(
    val workoutSessions: List<PortableWorkoutSession>,
    val workoutSets: List<PortableWorkoutSet>,
    val bodyMeasurements: List<PortableBodyMeasurement>,
    val foodItems: List<PortableFoodItem>,
    val foodEntries: List<PortableFoodEntry>,
    val progressPhotos: List<PortableProgressPhoto>,
    val programs: List<PortableProgram>,
    val programWorkouts: List<PortableProgramWorkout>,
    val programDayExercises: List<PortableProgramDayExercise>,
    val nutritionGoals: List<PortableNutritionGoal>,
    val seedMetadata: List<PortableSeedMetadata>
)

@Serializable
data class PortableWorkoutSession(
    val id: Long,
    val name: String,
    val date: Long,
    val durationMinutes: Int,
    val notes: String,
    val status: String,
    val completedAt: Long?
)

@Serializable
data class PortableWorkoutSet(
    val id: Long,
    val sessionId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean,
    val notes: String,
    val exerciseId: String?,
    val targetReps: String?,
    val exerciseOrder: Int
)

@Serializable
data class PortableBodyMeasurement(
    val id: Long,
    val date: Long,
    val weightKg: Float?,
    val bodyFatPercent: Float?,
    val chestCm: Float?,
    val waistCm: Float?,
    val hipsCm: Float?,
    val leftArmCm: Float?,
    val rightArmCm: Float?,
    val leftCalfCm: Float?,
    val rightCalfCm: Float?,
    val shouldersCm: Float?,
    val notes: String
)

@Serializable
data class PortableFoodItem(
    val id: Long,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val isCustom: Boolean
)

@Serializable
data class PortableFoodEntry(
    val id: Long,
    val date: Long,
    val foodItemId: Long,
    val foodName: String,
    val amountGrams: Float,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val mealType: String
)

@Serializable
data class PortableProgressPhoto(
    val id: Long,
    val date: Long,
    val weightKg: Float?,
    val notes: String,
    val angle: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storageVersion: Int,
    val widthPx: Int,
    val heightPx: Int,
    val mediaState: PortablePhotoMediaState,
    val mainAssetPath: String?,
    val thumbnailAssetPath: String?
)

@Serializable
enum class PortablePhotoMediaState {
    INCLUDED,
    LEGACY_METADATA_ONLY
}

@Serializable
data class PortableProgram(
    val id: Long,
    val name: String,
    val description: String,
    val durationWeeks: Int,
    val daysPerWeek: Int,
    val difficulty: String,
    val isPaid: Boolean,
    val price: Float,
    val isPurchased: Boolean,
    val coverImageUrl: String,
    val creatorName: String
)

@Serializable
data class PortableProgramWorkout(
    val id: Long,
    val programId: Long,
    val weekNumber: Int,
    val dayNumber: Int,
    val dayName: String,
    val exercises: String
)

@Serializable
data class PortableProgramDayExercise(
    val id: Long,
    val programWorkoutId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val position: Int,
    val targetSets: Int,
    val targetReps: String,
    val restSeconds: Int
)

@Serializable
data class PortableNutritionGoal(
    val id: Int,
    val dailyCalories: Int,
    val dailyProteinG: Int,
    val dailyCarbsG: Int,
    val dailyFatG: Int
)

@Serializable
data class PortableSeedMetadata(
    val seedKey: String,
    val contentVersion: Int
)

data class DatabaseSnapshot(
    val workoutSessions: List<WorkoutSession>,
    val workoutSets: List<WorkoutSet>,
    val bodyMeasurements: List<BodyMeasurement>,
    val foodItems: List<FoodItem>,
    val foodEntries: List<FoodEntry>,
    val progressPhotos: List<ProgressPhoto>,
    val programs: List<Program>,
    val programWorkouts: List<ProgramWorkout>,
    val programDayExercises: List<ProgramDayExercise>,
    val nutritionGoals: List<NutritionGoal>,
    val seedMetadata: List<SeedMetadata>
)

fun DatabaseSnapshot.toPortableData(): PortableData = PortableData(
    workoutSessions = workoutSessions.map { session ->
        PortableWorkoutSession(
            id = session.id,
            name = session.name,
            date = session.date,
            durationMinutes = session.durationMinutes,
            notes = session.notes,
            status = session.status.name,
            completedAt = session.completedAt
        )
    },
    workoutSets = workoutSets.map { set ->
        PortableWorkoutSet(
            id = set.id,
            sessionId = set.sessionId,
            exerciseName = set.exerciseName,
            setNumber = set.setNumber,
            reps = set.reps,
            weightKg = set.weightKg,
            isCompleted = set.isCompleted,
            notes = set.notes,
            exerciseId = set.exerciseId,
            targetReps = set.targetReps,
            exerciseOrder = set.exerciseOrder
        )
    },
    bodyMeasurements = bodyMeasurements.map { measurement ->
        PortableBodyMeasurement(
            id = measurement.id,
            date = measurement.date,
            weightKg = measurement.weightKg,
            bodyFatPercent = measurement.bodyFatPercent,
            chestCm = measurement.chestCm,
            waistCm = measurement.waistCm,
            hipsCm = measurement.hipsCm,
            leftArmCm = measurement.leftArmCm,
            rightArmCm = measurement.rightArmCm,
            leftCalfCm = measurement.leftCalfCm,
            rightCalfCm = measurement.rightCalfCm,
            shouldersCm = measurement.shouldersCm,
            notes = measurement.notes
        )
    },
    foodItems = foodItems.map { item ->
        PortableFoodItem(
            id = item.id,
            name = item.name,
            caloriesPer100g = item.caloriesPer100g,
            proteinPer100g = item.proteinPer100g,
            carbsPer100g = item.carbsPer100g,
            fatPer100g = item.fatPer100g,
            isCustom = item.isCustom
        )
    },
    foodEntries = foodEntries.map { entry ->
        PortableFoodEntry(
            id = entry.id,
            date = entry.date,
            foodItemId = entry.foodItemId,
            foodName = entry.foodName,
            amountGrams = entry.amountGrams,
            calories = entry.calories,
            protein = entry.protein,
            carbs = entry.carbs,
            fat = entry.fat,
            mealType = entry.mealType.name
        )
    },
    progressPhotos = progressPhotos.map { photo ->
        val isManaged = photo.storageVersion >= 1
        PortableProgressPhoto(
            id = photo.id,
            date = photo.date,
            weightKg = photo.weightKg,
            notes = photo.notes,
            angle = photo.angle.name,
            mimeType = photo.mimeType,
            sizeBytes = photo.sizeBytes,
            storageVersion = photo.storageVersion,
            widthPx = photo.widthPx,
            heightPx = photo.heightPx,
            mediaState = if (isManaged) {
                PortablePhotoMediaState.INCLUDED
            } else {
                PortablePhotoMediaState.LEGACY_METADATA_ONLY
            },
            mainAssetPath = if (isManaged) photoMainAssetPath(photo.id) else null,
            thumbnailAssetPath = if (isManaged && photo.thumbnailUri != null) {
                photoThumbnailAssetPath(photo.id)
            } else {
                null
            }
        )
    },
    programs = programs.map { program ->
        PortableProgram(
            id = program.id,
            name = program.name,
            description = program.description,
            durationWeeks = program.durationWeeks,
            daysPerWeek = program.daysPerWeek,
            difficulty = program.difficulty.name,
            isPaid = program.isPaid,
            price = program.price,
            isPurchased = program.isPurchased,
            coverImageUrl = program.coverImageUrl,
            creatorName = program.creatorName
        )
    },
    programWorkouts = programWorkouts.map { workout ->
        PortableProgramWorkout(
            id = workout.id,
            programId = workout.programId,
            weekNumber = workout.weekNumber,
            dayNumber = workout.dayNumber,
            dayName = workout.dayName,
            exercises = workout.exercises
        )
    },
    programDayExercises = programDayExercises.map { exercise ->
        PortableProgramDayExercise(
            id = exercise.id,
            programWorkoutId = exercise.programWorkoutId,
            exerciseId = exercise.exerciseId,
            exerciseName = exercise.exerciseName,
            position = exercise.position,
            targetSets = exercise.targetSets,
            targetReps = exercise.targetReps,
            restSeconds = exercise.restSeconds
        )
    },
    nutritionGoals = nutritionGoals.map { goal ->
        PortableNutritionGoal(
            id = goal.id,
            dailyCalories = goal.dailyCalories,
            dailyProteinG = goal.dailyProteinG,
            dailyCarbsG = goal.dailyCarbsG,
            dailyFatG = goal.dailyFatG
        )
    },
    seedMetadata = seedMetadata.map { metadata ->
        PortableSeedMetadata(
            seedKey = metadata.seedKey,
            contentVersion = metadata.contentVersion
        )
    }
)

fun photoMainAssetPath(photoId: Long): String {
    require(photoId > 0) { "Photo ID must be positive." }
    return "media/photos/$photoId/main.jpg"
}

fun photoThumbnailAssetPath(photoId: Long): String {
    require(photoId > 0) { "Photo ID must be positive." }
    return "media/photos/$photoId/thumbnail.jpg"
}
