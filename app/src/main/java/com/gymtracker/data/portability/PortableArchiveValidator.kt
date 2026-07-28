package com.gymtracker.data.portability

import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import com.gymtracker.data.settings.ThemeMode

class InvalidPortableArchiveException(message: String) : IllegalArgumentException(message)

data class RestoredPhotoUris(
    val mainUri: String,
    val thumbnailUri: String?
)

fun PortableArchive.validateForRestore(): PortableArchive {
    invalidIf(format != PORTABLE_ARCHIVE_FORMAT, "Unsupported archive format.")
    invalidIf(formatVersion != PORTABLE_ARCHIVE_VERSION, "Unsupported archive version.")
    invalidIf(
        databaseSchemaVersion != PORTABLE_DATABASE_SCHEMA_VERSION,
        "Unsupported database schema version."
    )
    enumValue<ThemeMode>(settings.themeMode, "theme mode")

    val lists = listOf(
        data.workoutSessions,
        data.workoutSets,
        data.bodyMeasurements,
        data.foodItems,
        data.foodEntries,
        data.progressPhotos,
        data.programs,
        data.programWorkouts,
        data.programDayExercises,
        data.nutritionGoals,
        data.seedMetadata
    )
    invalidIf(lists.any { it.size > MAX_RECORDS_PER_TABLE }, "Archive table is too large.")
    invalidIf(lists.sumOf { list -> list.size } > MAX_TOTAL_RECORDS, "Archive has too many records.")

    val sessionIds = positiveUniqueIds(data.workoutSessions.map { it.id }, "workout session")
    positiveUniqueIds(data.workoutSets.map { it.id }, "workout set")
    positiveUniqueIds(data.bodyMeasurements.map { it.id }, "body measurement")
    positiveUniqueIds(data.foodItems.map { it.id }, "food item")
    positiveUniqueIds(data.foodEntries.map { it.id }, "food entry")
    positiveUniqueIds(data.progressPhotos.map { it.id }, "progress photo")
    val programIds = positiveUniqueIds(data.programs.map { it.id }, "program")
    val programWorkoutIds =
        positiveUniqueIds(data.programWorkouts.map { it.id }, "program workout")
    positiveUniqueIds(data.programDayExercises.map { it.id }, "program exercise")

    data.workoutSessions.forEach { item ->
        text(item.name, "workout name")
        text(item.notes, "workout notes")
        enumValue<WorkoutStatus>(item.status, "workout status")
        invalidIf(item.durationMinutes < 0, "Workout duration cannot be negative.")
    }
    invalidIf(
        data.workoutSessions.count { item -> item.status == WorkoutStatus.ACTIVE.name } > 1,
        "Archive contains more than one active workout."
    )
    data.workoutSets.forEach { item ->
        invalidIf(item.sessionId !in sessionIds, "Workout set references a missing session.")
        text(item.exerciseName, "exercise name")
        text(item.notes, "set notes")
        item.exerciseId?.let { text(it, "exercise ID") }
        item.targetReps?.let { text(it, "target reps") }
        finiteNonNegative(item.weightKg, "set weight")
        invalidIf(item.setNumber < 0 || item.reps < 0 || item.exerciseOrder < 0, "Invalid set value.")
    }
    data.bodyMeasurements.forEach { item ->
        listOfNotNull(
            item.weightKg,
            item.bodyFatPercent,
            item.chestCm,
            item.waistCm,
            item.hipsCm,
            item.leftArmCm,
            item.rightArmCm,
            item.leftCalfCm,
            item.rightCalfCm,
            item.shouldersCm
        ).forEach { value -> finiteNonNegative(value, "measurement") }
        text(item.notes, "measurement notes")
    }
    data.foodItems.forEach { item ->
        text(item.name, "food name")
        finiteNonNegative(item.caloriesPer100g, "food calories")
        finiteNonNegative(item.proteinPer100g, "food protein")
        finiteNonNegative(item.carbsPer100g, "food carbs")
        finiteNonNegative(item.fatPer100g, "food fat")
    }
    data.foodEntries.forEach { item ->
        text(item.foodName, "food entry name")
        finiteNonNegative(item.amountGrams, "food amount")
        finiteNonNegative(item.calories, "entry calories")
        finiteNonNegative(item.protein, "entry protein")
        finiteNonNegative(item.carbs, "entry carbs")
        finiteNonNegative(item.fat, "entry fat")
        enumValue<MealType>(item.mealType, "meal type")
    }
    data.progressPhotos.forEach { item ->
        item.weightKg?.let { finiteNonNegative(it, "photo weight") }
        text(item.notes, "photo notes")
        text(item.mimeType, "photo MIME type")
        enumValue<PhotoAngle>(item.angle, "photo angle")
        invalidIf(
            item.sizeBytes < 0 || item.widthPx < 0 || item.heightPx < 0,
            "Invalid photo metadata."
        )
        when (item.mediaState) {
            PortablePhotoMediaState.INCLUDED -> {
                invalidIf(item.storageVersion < 1, "Included photo is not app-managed.")
                invalidIf(
                    item.mainAssetPath != photoMainAssetPath(item.id),
                    "Managed photo has an invalid main asset path."
                )
                invalidIf(
                    item.thumbnailAssetPath != null &&
                        item.thumbnailAssetPath != photoThumbnailAssetPath(item.id),
                    "Managed photo has an invalid thumbnail asset path."
                )
            }
            PortablePhotoMediaState.LEGACY_METADATA_ONLY -> {
                invalidIf(item.storageVersion != 0, "Legacy photo has an invalid storage version.")
                invalidIf(
                    item.mainAssetPath != null || item.thumbnailAssetPath != null,
                    "Legacy metadata-only photo declares media."
                )
            }
        }
    }
    data.programs.forEach { item ->
        text(item.name, "program name")
        text(item.description, "program description")
        text(item.coverImageUrl, "program cover")
        text(item.creatorName, "program creator")
        enumValue<ProgramDifficulty>(item.difficulty, "program difficulty")
        finiteNonNegative(item.price, "program price")
        invalidIf(item.durationWeeks < 0 || item.daysPerWeek < 0, "Invalid program duration.")
    }
    data.programWorkouts.forEach { item ->
        invalidIf(item.programId !in programIds, "Program workout references a missing program.")
        text(item.dayName, "program day name")
        text(item.exercises, "legacy program payload")
        invalidIf(item.weekNumber < 0 || item.dayNumber < 0, "Invalid program day.")
    }
    val positions = mutableSetOf<Pair<Long, Int>>()
    data.programDayExercises.forEach { item ->
        invalidIf(
            item.programWorkoutId !in programWorkoutIds,
            "Program exercise references a missing workout."
        )
        invalidIf(
            !positions.add(item.programWorkoutId to item.position),
            "Program exercise position is duplicated."
        )
        text(item.exerciseId, "program exercise ID")
        text(item.exerciseName, "program exercise name")
        text(item.targetReps, "program target reps")
        invalidIf(
            item.position < 0 || item.targetSets < 0 || item.restSeconds < 0,
            "Invalid program exercise target."
        )
    }
    invalidIf(
        data.nutritionGoals.size > 1 || data.nutritionGoals.any { it.id != 1 },
        "Nutrition goal identity is invalid."
    )
    data.nutritionGoals.forEach { item ->
        invalidIf(
            item.dailyCalories <= 0 ||
                item.dailyProteinG <= 0 ||
                item.dailyCarbsG <= 0 ||
                item.dailyFatG <= 0,
            "Nutrition goals must be positive."
        )
    }
    val seedKeys = mutableSetOf<String>()
    data.seedMetadata.forEach { item ->
        text(item.seedKey, "seed key")
        invalidIf(!seedKeys.add(item.seedKey), "Seed key is duplicated.")
        invalidIf(item.contentVersion < 0, "Seed version cannot be negative.")
    }
    return this
}

fun PortableArchive.expectedMediaPaths(): Set<String> =
    data.progressPhotos.flatMapTo(linkedSetOf()) { photo ->
        listOfNotNull(photo.mainAssetPath, photo.thumbnailAssetPath)
    }

fun PortableArchive.toDatabaseSnapshot(
    restoredPhotoUris: Map<Long, RestoredPhotoUris>
): DatabaseSnapshot {
    validateForRestore()
    return DatabaseSnapshot(
        workoutSessions = data.workoutSessions.map { item ->
            WorkoutSession(
                id = item.id,
                name = item.name,
                date = item.date,
                durationMinutes = item.durationMinutes,
                notes = item.notes,
                status = enumValue(item.status, "workout status"),
                completedAt = item.completedAt
            )
        },
        workoutSets = data.workoutSets.map { item ->
            WorkoutSet(
                id = item.id,
                sessionId = item.sessionId,
                exerciseName = item.exerciseName,
                setNumber = item.setNumber,
                reps = item.reps,
                weightKg = item.weightKg,
                isCompleted = item.isCompleted,
                notes = item.notes,
                exerciseId = item.exerciseId,
                targetReps = item.targetReps,
                exerciseOrder = item.exerciseOrder
            )
        },
        bodyMeasurements = data.bodyMeasurements.map { item ->
            BodyMeasurement(
                id = item.id,
                date = item.date,
                weightKg = item.weightKg,
                bodyFatPercent = item.bodyFatPercent,
                chestCm = item.chestCm,
                waistCm = item.waistCm,
                hipsCm = item.hipsCm,
                leftArmCm = item.leftArmCm,
                rightArmCm = item.rightArmCm,
                leftCalfCm = item.leftCalfCm,
                rightCalfCm = item.rightCalfCm,
                shouldersCm = item.shouldersCm,
                notes = item.notes
            )
        },
        foodItems = data.foodItems.map { item ->
            FoodItem(
                id = item.id,
                name = item.name,
                caloriesPer100g = item.caloriesPer100g,
                proteinPer100g = item.proteinPer100g,
                carbsPer100g = item.carbsPer100g,
                fatPer100g = item.fatPer100g,
                isCustom = item.isCustom
            )
        },
        foodEntries = data.foodEntries.map { item ->
            FoodEntry(
                id = item.id,
                date = item.date,
                foodItemId = item.foodItemId,
                foodName = item.foodName,
                amountGrams = item.amountGrams,
                calories = item.calories,
                protein = item.protein,
                carbs = item.carbs,
                fat = item.fat,
                mealType = enumValue(item.mealType, "meal type")
            )
        },
        progressPhotos = data.progressPhotos.map { item ->
            val restored = restoredPhotoUris[item.id]
            invalidIf(
                item.mediaState == PortablePhotoMediaState.INCLUDED && restored == null,
                "Managed photo media was not staged."
            )
            ProgressPhoto(
                id = item.id,
                date = item.date,
                photoUri = restored?.mainUri ?: "$LEGACY_RESTORED_URI_PREFIX${item.id}",
                weightKg = item.weightKg,
                notes = item.notes,
                angle = enumValue(item.angle, "photo angle"),
                mimeType = item.mimeType,
                sizeBytes = item.sizeBytes,
                storageVersion = if (restored == null) 0 else item.storageVersion,
                thumbnailUri = restored?.thumbnailUri,
                widthPx = item.widthPx,
                heightPx = item.heightPx
            )
        },
        programs = data.programs.map { item ->
            Program(
                id = item.id,
                name = item.name,
                description = item.description,
                durationWeeks = item.durationWeeks,
                daysPerWeek = item.daysPerWeek,
                difficulty = enumValue(item.difficulty, "program difficulty"),
                isPaid = item.isPaid,
                price = item.price,
                isPurchased = item.isPurchased,
                coverImageUrl = item.coverImageUrl,
                creatorName = item.creatorName
            )
        },
        programWorkouts = data.programWorkouts.map { item ->
            ProgramWorkout(
                id = item.id,
                programId = item.programId,
                weekNumber = item.weekNumber,
                dayNumber = item.dayNumber,
                dayName = item.dayName,
                exercises = item.exercises
            )
        },
        programDayExercises = data.programDayExercises.map { item ->
            ProgramDayExercise(
                id = item.id,
                programWorkoutId = item.programWorkoutId,
                exerciseId = item.exerciseId,
                exerciseName = item.exerciseName,
                position = item.position,
                targetSets = item.targetSets,
                targetReps = item.targetReps,
                restSeconds = item.restSeconds
            )
        },
        nutritionGoals = data.nutritionGoals.map { item ->
            NutritionGoal(
                id = item.id,
                dailyCalories = item.dailyCalories,
                dailyProteinG = item.dailyProteinG,
                dailyCarbsG = item.dailyCarbsG,
                dailyFatG = item.dailyFatG
            )
        },
        seedMetadata = data.seedMetadata.map { item ->
            SeedMetadata(seedKey = item.seedKey, contentVersion = item.contentVersion)
        }
    )
}

private fun positiveUniqueIds(ids: List<Long>, label: String): Set<Long> {
    invalidIf(ids.any { it <= 0 }, "$label ID must be positive.")
    val unique = ids.toSet()
    invalidIf(unique.size != ids.size, "$label ID is duplicated.")
    return unique
}

private fun text(value: String, label: String) {
    invalidIf(value.length > MAX_TEXT_LENGTH, "$label is too long.")
}

private fun finiteNonNegative(value: Float, label: String) {
    invalidIf(!value.isFinite() || value < 0f, "$label is invalid.")
}

private inline fun <reified T : Enum<T>> enumValue(value: String, label: String): T =
    enumValues<T>().firstOrNull { it.name == value }
        ?: throw InvalidPortableArchiveException("Unknown $label.")

private fun invalidIf(condition: Boolean, message: String) {
    if (condition) throw InvalidPortableArchiveException(message)
}

private const val MAX_RECORDS_PER_TABLE = 100_000
private const val MAX_TOTAL_RECORDS = 250_000
private const val MAX_TEXT_LENGTH = 100_000
private const val LEGACY_RESTORED_URI_PREFIX = "gymtracker://restored/legacy-photo/"
