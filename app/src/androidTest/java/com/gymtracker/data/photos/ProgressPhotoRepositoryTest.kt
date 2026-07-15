package com.gymtracker.data.photos

import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.dao.ProgressPhotoDao
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
        reader.bytes = createJpeg(width = 800, height = 400)

        val result = repository.importPhoto(
            Uri.parse("content://test/photo"),
            PhotoAngle.FRONT,
            80.5f,
            "check-in"
        )

        assertTrue(result.isSuccess)
        val photo = database.progressPhotoDao().getAllSync().single()
        val managedFile = java.io.File(requireNotNull(Uri.parse(photo.photoUri).path))
        val thumbnailFile = java.io.File(requireNotNull(Uri.parse(photo.thumbnailUri).path))
        assertTrue(managedFile.exists())
        assertTrue(thumbnailFile.exists())
        assertEquals(managedFile.length(), photo.sizeBytes)
        assertEquals("image/jpeg", photo.mimeType)
        assertEquals(2, photo.storageVersion)
        assertEquals(800, photo.widthPx)
        assertEquals(400, photo.heightPx)
        val thumbnailBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(thumbnailFile.path, thumbnailBounds)
        assertEquals(512, thumbnailBounds.outWidth)
        assertEquals(256, thumbnailBounds.outHeight)

        // A new storage instance can still resolve the app-owned file after a restart.
        PhotoFileStorage(context)
        assertTrue(managedFile.exists())

        assertTrue(repository.deletePhoto(photo).isSuccess)
        assertFalse(managedFile.exists())
        assertFalse(thumbnailFile.exists())
        assertTrue(database.progressPhotoDao().getAllSync().isEmpty())
    }

    @Test
    fun importPhoto_appliesExifOrientationBeforeWritingMetadata() = kotlinx.coroutines.runBlocking {
        reader.bytes = createJpegWithOrientation(
            width = 40,
            height = 20,
            orientation = ExifInterface.ORIENTATION_ROTATE_90
        )

        val result = repository.importPhoto(
            Uri.parse("content://test/oriented-photo"),
            PhotoAngle.SIDE_RIGHT,
            null,
            ""
        )

        assertTrue(result.isSuccess)
        val photo = database.progressPhotoDao().getAllSync().single()
        assertEquals(20, photo.widthPx)
        assertEquals(40, photo.heightPx)
        assertTrue(repository.deletePhoto(photo).isSuccess)
    }

    @Test
    fun importPhoto_whenDecodeFails_cleansTemporaryFilesAndDoesNotCreateRow() =
        kotlinx.coroutines.runBlocking {
            reader.bytes = "not-an-image".toByteArray()
            val directory = File(context.filesDir, "progress_photos")
            val filesBefore = directory.listFiles()?.map(File::getName)?.sorted().orEmpty()

            val result = repository.importPhoto(
                Uri.parse("content://test/corrupt-image"),
                PhotoAngle.FRONT,
                null,
                ""
            )

            assertTrue(result.isFailure)
            assertTrue(database.progressPhotoDao().getAllSync().isEmpty())
            assertEquals(filesBefore, directory.listFiles()?.map(File::getName)?.sorted().orEmpty())
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
        reader.bytes = createJpeg(width = 64, height = 64)
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

    @Test
    fun reconcileStorage_removesOrphanAndMissingManagedRowButKeepsLegacyRow() =
        kotlinx.coroutines.runBlocking {
            val directory = File(context.filesDir, "progress_photos").apply { mkdirs() }
            File(directory, "orphan.jpg").writeBytes("orphan".toByteArray())
            val thumbnailForMissingMain = File(directory, "missing-main-thumb.jpg").apply {
                writeBytes(createJpeg(width = 32, height = 32))
            }
            database.progressPhotoDao().insert(
                ProgressPhoto(
                    photoUri = Uri.fromFile(File(directory, "missing.jpg")).toString(),
                    thumbnailUri = Uri.fromFile(thumbnailForMissingMain).toString(),
                    storageVersion = 2
                )
            )
            database.progressPhotoDao().insert(
                ProgressPhoto(
                    photoUri = "content://legacy/photo",
                    storageVersion = 0
                )
            )
            val retainedMain = File(directory, "retained.jpg").apply {
                writeBytes(createJpeg(width = 32, height = 32))
            }
            database.progressPhotoDao().insert(
                ProgressPhoto(
                    photoUri = Uri.fromFile(retainedMain).toString(),
                    thumbnailUri = Uri.fromFile(File(directory, "missing-thumb.jpg")).toString(),
                    storageVersion = 2
                )
            )

            val result = repository.reconcileStorage()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().orphanFilesDeleted)
            assertEquals(1, result.getOrThrow().missingRowsDeleted)
            assertEquals(1, result.getOrThrow().missingThumbnailsCleared)
            assertEquals(
                setOf("content://legacy/photo", Uri.fromFile(retainedMain).toString()),
                database.progressPhotoDao().getAllSync().map { it.photoUri }.toSet()
            )
            assertEquals(
                null,
                database.progressPhotoDao().getAllSync()
                    .first { it.photoUri == Uri.fromFile(retainedMain).toString() }
                    .thumbnailUri
            )
            assertFalse(File(directory, "orphan.jpg").exists())
            assertFalse(thumbnailForMissingMain.exists())
            retainedMain.delete()
            Unit
        }

    private class FakePhotoSourceReader : PhotoSourceReader {
        var bytes: ByteArray = ByteArray(0)
        var failure: Throwable? = null

        override fun open(uri: Uri): OpenedPhotoSource {
            failure?.let { throw it }
            return OpenedPhotoSource(ByteArrayInputStream(bytes), "image/jpeg")
        }
    }

    private fun createJpeg(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(30, 90, 160))
        return ByteArrayOutputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }

    private fun createJpegWithOrientation(width: Int, height: Int, orientation: Int): ByteArray {
        val file = File.createTempFile("exif-test-", ".jpg", context.cacheDir)
        return try {
            file.writeBytes(createJpeg(width, height))
            ExifInterface(file.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
            file.readBytes()
        } finally {
            file.delete()
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

        override suspend fun update(photo: ProgressPhoto) = Unit

        override suspend fun delete(photo: ProgressPhoto) = Unit
    }
}
