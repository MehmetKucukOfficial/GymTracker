

package com.gymtracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.gymtracker.data.dao.*
import com.gymtracker.data.entities.*
import com.gymtracker.data.portability.DataPortabilityDao

class Converters {
    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)

    @TypeConverter
    fun fromPhotoAngle(value: PhotoAngle): String = value.name

    @TypeConverter
    fun toPhotoAngle(value: String): PhotoAngle = PhotoAngle.valueOf(value)

    @TypeConverter
    fun fromDifficulty(value: ProgramDifficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): ProgramDifficulty = ProgramDifficulty.valueOf(value)

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String = value.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)
}

@Database(
    entities = [
        WorkoutSession::class,
        WorkoutSet::class,
        BodyMeasurement::class,
        FoodItem::class,
        FoodEntry::class,
        ProgressPhoto::class,
        Program::class,
        ProgramWorkout::class,
        ProgramDayExercise::class,
        NutritionGoal::class,
        SeedMetadata::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun programDao(): ProgramDao
    abstract fun dataPortabilityDao(): DataPortabilityDao
}
