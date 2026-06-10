package com.gymtracker.di

import android.content.Context
import androidx.room.Room
import com.gymtracker.data.dao.*
import com.gymtracker.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "gymtracker.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideMeasurementDao(db: AppDatabase): MeasurementDao = db.measurementDao()
    @Provides fun provideNutritionDao(db: AppDatabase): NutritionDao = db.nutritionDao()
    @Provides fun provideProgressPhotoDao(db: AppDatabase): ProgressPhotoDao = db.progressPhotoDao()
    @Provides fun provideProgramDao(db: AppDatabase): ProgramDao = db.programDao()
}
