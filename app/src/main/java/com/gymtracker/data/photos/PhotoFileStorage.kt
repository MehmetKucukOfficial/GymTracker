package com.gymtracker.data.photos

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class StoredPhoto(
    val uri: String,
    val mimeType: String,
    val sizeBytes: Long
)

data class PhotoStorageReconciliation(
    val orphanFilesDeleted: Int,
    val missingManagedUris: Set<String>
)

@Singleton
class PhotoFileStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val directory = File(context.filesDir, DIRECTORY_NAME)

    init {
        directory.mkdirs()
        directory.listFiles { file -> file.name.endsWith(TEMP_SUFFIX) }
            ?.forEach(File::delete)
    }

    fun store(input: InputStream, mimeType: String): StoredPhoto {
        check(directory.exists() || directory.mkdirs()) { "Fotoğraf klasörü oluşturulamadı." }
        val tempFile = File.createTempFile("import-", TEMP_SUFFIX, directory)
        try {
            val size = tempFile.outputStream().buffered().use { output ->
                copyWithLimit(input, output)
            }
            require(size > 0) { "Seçilen görsel boş." }

            val extension = extensionFor(mimeType)
            val destination = File(directory, "${UUID.randomUUID()}.$extension")
            check(tempFile.renameTo(destination)) { "Fotoğraf kalıcı konuma taşınamadı." }
            return StoredPhoto(Uri.fromFile(destination).toString(), mimeType, size)
        } catch (error: Throwable) {
            tempFile.delete()
            throw error
        }
    }

    fun deleteManaged(uriValue: String): Boolean {
        val uri = Uri.parse(uriValue)
        if (uri.scheme != "file") return true
        val path = uri.path ?: return true
        val file = File(path)
        val managedRoot = directory.canonicalFile
        val candidate = file.canonicalFile
        if (candidate.parentFile != managedRoot) return false
        return !candidate.exists() || candidate.delete()
    }

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

    private fun copyWithLimit(input: InputStream, output: java.io.OutputStream): Long {
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

    private fun extensionFor(mimeType: String) = when (mimeType.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/heic", "image/heif" -> "heic"
        else -> "jpg"
    }

    private companion object {
        const val DIRECTORY_NAME = "progress_photos"
        const val TEMP_SUFFIX = ".tmp"
        const val MAX_FILE_BYTES = 25L * 1024L * 1024L
    }
}
