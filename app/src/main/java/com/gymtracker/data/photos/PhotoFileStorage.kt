package com.gymtracker.data.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

data class StoredPhoto(
    val uri: String,
    val thumbnailUri: String,
    val mimeType: String,
    val sizeBytes: Long,
    val widthPx: Int,
    val heightPx: Int
)

data class PhotoStorageReconciliation(
    val orphanFilesDeleted: Int,
    val missingManagedUris: Set<String>
)

class InsufficientPhotoStorageException(
    val requiredBytes: Long,
    val availableBytes: Long
) : IOException("Progress photo import requires $requiredBytes bytes; $availableBytes bytes available.")

@Singleton
class PhotoFileStorage private constructor(
    context: Context,
    private val availableBytes: (File) -> Long
) {
    @Inject
    constructor(
        @ApplicationContext context: Context
    ) : this(context, { directory -> StatFs(directory.path).availableBytes })

    internal constructor(
        context: Context,
        availableBytes: Long
    ) : this(context, { availableBytes })

    private val directory = File(context.filesDir, DIRECTORY_NAME)

    init {
        directory.mkdirs()
        directory.listFiles { file -> file.name.endsWith(TEMP_SUFFIX) }
            ?.forEach(File::delete)
    }

    /**
     * Copies the provider stream first, then decodes it with bounded memory, applies EXIF
     * orientation and atomically publishes a display image plus a small grid thumbnail.
     */
    fun store(input: InputStream, mimeType: String): StoredPhoto {
        check(directory.exists() || directory.mkdirs()) { "Fotoğraf klasörü oluşturulamadı." }
        require(mimeType.startsWith("image/")) { "Seçilen dosya bir görsel değil." }
        val freeBytes = availableBytes(directory)
        if (freeBytes < REQUIRED_FREE_BYTES) {
            throw InsufficientPhotoStorageException(REQUIRED_FREE_BYTES, freeBytes)
        }
        val sourceTemp = File.createTempFile("source-", TEMP_SUFFIX, directory)
        val mainTemp = File.createTempFile("main-", TEMP_SUFFIX, directory)
        val thumbnailTemp = File.createTempFile("thumb-", TEMP_SUFFIX, directory)
        var destination: File? = null
        var thumbnailDestination: File? = null
        var decoded: Bitmap? = null
        var oriented: Bitmap? = null
        var display: Bitmap? = null
        var thumbnail: Bitmap? = null
        try {
            sourceTemp.outputStream().buffered().use { output -> copyWithLimit(input, output) }
            require(sourceTemp.length() > 0L) { "Seçilen görsel boş." }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceTemp.path, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Görsel çözümlenemedi." }
            require(
                bounds.outWidth <= MAX_SOURCE_DIMENSION && bounds.outHeight <= MAX_SOURCE_DIMENSION
            ) { "Görsel boyutları desteklenen sınırı aşıyor." }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_DISPLAY_DIMENSION)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            decoded = requireNotNull(BitmapFactory.decodeFile(sourceTemp.path, options)) {
                "Görsel çözümlenemedi."
            }
            val orientation = runCatching {
                ExifInterface(sourceTemp.path).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            oriented = applyExifOrientation(decoded, orientation)
            display = scaleDown(oriented, MAX_DISPLAY_DIMENSION)
            thumbnail = scaleDown(display, THUMBNAIL_DIMENSION)

            writeJpeg(display, mainTemp, MAIN_JPEG_QUALITY)
            writeJpeg(thumbnail, thumbnailTemp, THUMBNAIL_JPEG_QUALITY)

            val baseName = UUID.randomUUID().toString()
            destination = File(directory, "$baseName.jpg")
            thumbnailDestination = File(directory, "$baseName-thumb.jpg")
            check(mainTemp.renameTo(destination)) { "Fotoğraf kalıcı konuma taşınamadı." }
            check(thumbnailTemp.renameTo(thumbnailDestination)) { "Küçük görsel kalıcı konuma taşınamadı." }

            return StoredPhoto(
                uri = Uri.fromFile(destination).toString(),
                thumbnailUri = Uri.fromFile(thumbnailDestination).toString(),
                mimeType = OUTPUT_MIME_TYPE,
                sizeBytes = destination.length(),
                widthPx = display.width,
                heightPx = display.height
            )
        } catch (error: Throwable) {
            destination?.delete()
            thumbnailDestination?.delete()
            throw error
        } finally {
            sourceTemp.delete()
            mainTemp.delete()
            thumbnailTemp.delete()
            listOfNotNull(thumbnail, display, oriented, decoded)
                .distinctBy { bitmap -> System.identityHashCode(bitmap) }
                .forEach { bitmap -> if (!bitmap.isRecycled) bitmap.recycle() }
        }
    }

    fun deleteManaged(uriValue: String?): Boolean {
        if (uriValue == null) return true
        if (Uri.parse(uriValue).scheme != "file") return true
        val candidate = resolveManagedFile(uriValue) ?: return false
        return !candidate.exists() || candidate.delete()
    }

    fun openManaged(uriValue: String): InputStream? =
        resolveManagedFile(uriValue)
            ?.takeIf(File::isFile)
            ?.inputStream()
            ?.buffered()

    fun reconcile(knownManagedUris: Set<String>): PhotoStorageReconciliation {
        check(directory.exists() || directory.mkdirs()) { "Fotoğraf klasörü oluşturulamadı." }
        val managedRoot = directory.canonicalFile
        val knownFiles = knownManagedUris.associateWith { uriValue ->
            runCatching {
                val uri = Uri.parse(uriValue)
                val path = uri.path ?: return@runCatching null
                File(path).canonicalFile.takeIf { candidate ->
                    uri.scheme == "file" && candidate.parentFile == managedRoot
                }
            }.getOrNull()
        }
        val missing = knownFiles
            .filterValues { file -> file == null || !file.isFile }
            .keys
        val retainedPaths = knownFiles.values.filterNotNull().mapTo(mutableSetOf(), File::getPath)
        var deleted = 0
        directory.listFiles()?.forEach { file ->
            val shouldDelete = file.isFile &&
                (file.name.endsWith(TEMP_SUFFIX) || file.canonicalPath !in retainedPaths)
            if (shouldDelete && file.delete()) deleted++
        }
        return PhotoStorageReconciliation(
            orphanFilesDeleted = deleted,
            missingManagedUris = missing
        )
    }

    private fun copyWithLimit(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= MAX_FILE_BYTES) { "Görsel boyutu 25 MB sınırını aşıyor." }
            output.write(buffer, 0, read)
        }
        return total
    }

    private fun resolveManagedFile(uriValue: String): File? {
        val uri = Uri.parse(uriValue)
        if (uri.scheme != "file") return null
        val path = uri.path ?: return null
        val managedRoot = directory.canonicalFile
        val candidate = File(path).canonicalFile
        return candidate.takeIf { file -> file.parentFile == managedRoot }
    }

    private fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        while (max(width, height) / (sample * 2) >= maxDimension) sample *= 2
        return sample
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / largest.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
            (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
            true
        )
    }

    private fun writeJpeg(bitmap: Bitmap, file: File, quality: Int) {
        file.outputStream().buffered().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                "Görsel optimize edilemedi."
            }
        }
        check(file.length() > 0L) { "Görsel optimize edilemedi." }
    }

    private companion object {
        const val DIRECTORY_NAME = "progress_photos"
        const val TEMP_SUFFIX = ".tmp"
        const val OUTPUT_MIME_TYPE = "image/jpeg"
        const val MAX_FILE_BYTES = 25L * 1024L * 1024L
        const val REQUIRED_FREE_BYTES = 48L * 1024L * 1024L
        const val MAX_SOURCE_DIMENSION = 20_000
        const val MAX_DISPLAY_DIMENSION = 2_048
        const val THUMBNAIL_DIMENSION = 512
        const val MAIN_JPEG_QUALITY = 90
        const val THUMBNAIL_JPEG_QUALITY = 82
    }
}
