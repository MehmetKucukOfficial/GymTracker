package com.gymtracker.data.portability

import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.photos.PhotoFileStorage
import com.gymtracker.data.settings.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Narrow application-component access for host-orchestrated acceptance fixtures.
 *
 * The fixtures must use the same Room, DataStore, archive and managed-media singletons as the
 * target process so an actual OS process kill exercises production persistence boundaries.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface DataPortabilityEntryPoint {
    fun database(): AppDatabase
    fun settingsRepository(): SettingsRepository
    fun dataExportRepository(): DataExportRepository
    fun archiveReader(): PortableArchiveReader
    fun photoFileStorage(): PhotoFileStorage
}
