package com.gymtracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    val ALL: Array<Migration> = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
}
