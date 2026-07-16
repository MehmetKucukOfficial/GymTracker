package com.gymtracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymtracker.domain.ProgramExerciseSerializer

/**
 * Single registry for every supported Room migration edge.
 *
 * Version 3 is the oldest schema available in source control. Add each future
 * migration here and cover every supported path with MigrationTestHelper.
 */
object DatabaseMigrations {
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `seed_metadata` (
                    `seedKey` TEXT NOT NULL,
                    `contentVersion` INTEGER NOT NULL,
                    PRIMARY KEY(`seedKey`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `progress_photos` ADD COLUMN `mimeType` TEXT NOT NULL DEFAULT 'image/*'")
            db.execSQL("ALTER TABLE `progress_photos` ADD COLUMN `sizeBytes` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `progress_photos` ADD COLUMN `storageVersion` INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'ACTIVE'"
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `completedAt` INTEGER"
            )
            db.execSQL(
                """
                UPDATE `workout_sessions`
                SET `status` = 'COMPLETED',
                    `completedAt` = `date` + (`durationMinutes` * 60000)
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_sessions_status` ON `workout_sessions` (`status`)"
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `workout_sets` ADD COLUMN `exerciseId` TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE `workout_sets` ADD COLUMN `targetReps` TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE `workout_sets` ADD COLUMN `exerciseOrder` INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `program_day_exercises` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `programWorkoutId` INTEGER NOT NULL,
                    `exerciseId` TEXT NOT NULL,
                    `exerciseName` TEXT NOT NULL,
                    `position` INTEGER NOT NULL,
                    `targetSets` INTEGER NOT NULL,
                    `targetReps` TEXT NOT NULL,
                    `restSeconds` INTEGER NOT NULL,
                    FOREIGN KEY(`programWorkoutId`) REFERENCES `program_workouts`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_program_day_exercises_programWorkoutId` " +
                    "ON `program_day_exercises` (`programWorkoutId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_program_day_exercises_programWorkoutId_position` " +
                    "ON `program_day_exercises` (`programWorkoutId`, `position`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_program_day_exercises_exerciseId` " +
                    "ON `program_day_exercises` (`exerciseId`)"
            )

            val insert = db.compileStatement(
                """
                INSERT INTO `program_day_exercises`
                    (`programWorkoutId`, `exerciseId`, `exerciseName`, `position`,
                     `targetSets`, `targetReps`, `restSeconds`)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            )
            db.query("SELECT `id`, `exercises` FROM `program_workouts`").use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val exercisesIndex = cursor.getColumnIndexOrThrow("exercises")
                while (cursor.moveToNext()) {
                    val workoutId = cursor.getLong(idIndex)
                    val legacyJson = cursor.getString(exercisesIndex).orEmpty()
                    val exercises = runCatching {
                        ProgramExerciseSerializer.fromJson(legacyJson)
                    }.getOrDefault(emptyList())
                    exercises.forEachIndexed { position, exercise ->
                        insert.clearBindings()
                        insert.bindLong(1, workoutId)
                        insert.bindString(2, exercise.exerciseId)
                        insert.bindString(3, exercise.name)
                        insert.bindLong(4, position.toLong())
                        insert.bindLong(5, exercise.sets.toLong())
                        insert.bindString(6, exercise.reps)
                        insert.bindLong(7, exercise.restSeconds.toLong())
                        insert.executeInsert()
                    }
                }
            }
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_body_measurements_date` " +
                    "ON `body_measurements` (`date`)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8
    )
}
