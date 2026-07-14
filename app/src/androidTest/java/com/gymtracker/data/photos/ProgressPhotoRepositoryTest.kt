package com.gymtracker.data.photos

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.dao.ProgressPhotoDao
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressPhotoRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var reader: FakePhotoSourceReader
    private lateinit var storage: PhotoFileStorage
    private lateinit var repository: ProgressPhotoRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reader = FakePhotoSourceReader()
        storage = PhotoFileStorage(context)
        repository = ProgressPhotoRepository(database.progressPhotoDao(), reader, storage)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun importPhoto_copiesToManagedStorageAndDeleteCleansFileAndRow() = kotlinx.coroutines.runBlocking {
        reader.bytes = "durable-image-content".toByteArray()

        val result = repository.importPhoto(
            Uri.parse("content://test/photo"),
            PhotoAngle.FRONT,
            80.5f,
            "check-in"
        )

        assertTrue(result.isSuccess)
        val photo = database.progressPhotoDao().getAllSync().single()
        val managedFile = java.io.File(requireNotNull(Uri.parse(photo.photoUri).path))
        assertTrue(managedFile.exists())
        assertEquals(reader.bytes.size.toLong(), photo.sizeBytes)
        assertEquals("image/jpeg", photo.mimeType)
        assertEquals(1, photo.storageVersion)

        // A new storage instance can still resolve the app-owned file after a restart.
        PhotoFileStorage(context)
        assertTrue(managedFile.exists())

        assertTrue(repository.deletePhoto(photo).isSuccess)
        assertFalse(managedFile.exists())
        assertTrue(database.progressPhotoDao().getAllSync().isEmpty())
    }

    @Test
    fun importPhoto_whenSourceFails_doesNotCreateDatabaseRow() = kotlinx.coroutines.runBlocking {
        reader.failure = IOException("provider access lost")

        val result = repository.importPhoto(
            Uri.parse("content://test/broken"),
            PhotoAngle.BACK,
            null,
            ""
        )

        assertTrue(result.isFailure)
        assertTrue(database.progressPhotoDao().getAllSync().isEmpty())
    }

    @Test
    fun importPhoto_whenDatabaseInsertFails_removesCopiedFile() = kotlinx.coroutines.runBlocking {
        reader.bytes = "copy-before-database-failure".toByteArray()
        val directory = File(context.filesDir, "progress_photos")
        val filesBefore = directory.listFiles()?.map(File::getName)?.sorted().orEmpty()
        val failingRepository = ProgressPhotoRepository(FailingInsertDao(), reader, storage)

        val result = failingRepository.importPhoto(
            Uri.parse("content://test/database-failure"),
            PhotoAngle.SIDE_LEFT,
            null,
            ""
        )

        assertTrue(result.isFailure)
        assertEquals(filesBefore, directory.listFiles()?.map(File::getName)?.sorted().orEmpty())
    }

    @Test
    fun store_whenSourceExceedsLimit_removesTemporaryFile() {
        val directory = File(context.filesDir, "progress_photos")
        val filesBefore = directory.listFiles()?.map(File::getName)?.sorted().orEmpty()
        val result = runCatching {
            storage.store(GeneratedInputStream(25L * 1024L * 1024L + 1L), "image/jpeg")
        }

        assertTrue(result.isFailure)
        assertEquals(filesBefore, directory.listFiles()?.map(File::getName)?.sorted().orEmpty())
    }

    private class FakePhotoSourceReader : PhotoSourceReader {
        var bytes: ByteArray = ByteArray(0)
        var failure: Throwable? = null

        override fun open(uri: Uri): OpenedPhotoSource {
            failure?.let { throw it }
            return OpenedPhotoSource(ByteArrayInputStream(bytes), "image/jpeg")
        }
    }

    private class GeneratedInputStream(private var remaining: Long) : InputStream() {
        override fun read(): Int {
            if (remaining == 0L) return -1
            remaining--
            return 0
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (remaining == 0L) return -1
            val count = minOf(length.toLong(), remaining).toInt()
            buffer.fill(0, offset, offset + count)
            remaining -= count
            return count
        }
    }

    private class FailingInsertDao : ProgressPhotoDao {
        override fun getAll(): Flow<List<ProgressPhoto>> = flowOf(emptyList())

        override fun getByAngle(angle: PhotoAngle): Flow<List<ProgressPhoto>> = flowOf(emptyList())

        override fun getSince(since: Long): Flow<List<ProgressPhoto>> = flowOf(emptyList())

        override suspend fun getAllSync(): List<ProgressPhoto> = emptyList()

        override suspend fun insert(photo: ProgressPhoto): Long = throw IOException("database full")

        override suspend fun delete(photo: ProgressPhoto) = Unit
    }
}
