package com.gymtracker.data.portability

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CorruptPortableArchiveException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

data class StagedPortableArchive(
    val archive: PortableArchive,
    val mediaFiles: Map<String, File>,
    private val directory: File
) : Closeable {
    override fun close() {
        directory.deleteRecursively()
    }
}

@Singleton
class PortableArchiveReader @Inject constructor() {
    private val json = Json {
        ignoreUnknownKeys = false
    }

    fun stage(archiveFile: File, stagingDirectory: File): StagedPortableArchive {
        check(stagingDirectory.mkdirs()) { "Restore staging directory could not be created." }
        try {
            ZipFile(archiveFile).use { zip ->
                val entries = zip.entries()
                    .asSequence()
                    .take(MAX_ARCHIVE_ENTRIES + 1)
                    .toList()
                corruptIf(
                    entries.size > MAX_ARCHIVE_ENTRIES,
                    "Archive contains too many entries."
                )
                val names = entries.map(ZipEntry::getName)
                corruptIf(names.size != names.toSet().size, "Archive has duplicate entries.")
                corruptIf(entries.any(ZipEntry::isDirectory), "Archive contains a directory entry.")
                entries.forEach { entry -> safeArchivePath(entry.name) }

                val dataEntry = zip.getEntry(PORTABLE_DATA_ENTRY)
                    ?: throw CorruptPortableArchiveException("Archive data is missing.")
                val archive = try {
                    val bytes = zip.getInputStream(dataEntry).use { input ->
                        input.readBounded(MAX_DATA_BYTES, "Archive data is too large.")
                    }
                    json.decodeFromString<PortableArchive>(bytes.decodeToString())
                        .validateForRestore()
                } catch (error: InvalidPortableArchiveException) {
                    throw error
                } catch (error: Exception) {
                    throw CorruptPortableArchiveException("Archive data is invalid.", error)
                }

                val expected = archive.expectedMediaPaths()
                val actual = names.toSet() - PORTABLE_DATA_ENTRY
                corruptIf(actual != expected, "Archive media entries do not match metadata.")
                val expectedEntries = expected.associateWith { path ->
                    requireNotNull(zip.getEntry(path))
                }
                val totalMediaBytes = expectedEntries.values.sumOf(ZipEntry::getSize)
                corruptIf(
                    expectedEntries.values.any { entry ->
                        entry.size !in 1..MAX_RESTORED_MEDIA_BYTES || entry.crc < 0
                    } ||
                        totalMediaBytes > MAX_TOTAL_RESTORED_MEDIA_BYTES,
                    "Archive media exceeds the safety limit."
                )
                val root = stagingDirectory.canonicalFile
                val requiredWorkingBytes = totalMediaBytes
                    .times(2)
                    .plus(RESTORE_WORKING_SAFETY_BYTES)
                val availableWorkingBytes = root.parentFile?.usableSpace ?: 0L
                if (availableWorkingBytes < requiredWorkingBytes) {
                    throw InsufficientRestoreStorageException(
                        requiredBytes = requiredWorkingBytes,
                        availableBytes = availableWorkingBytes
                    )
                }
                val media = linkedMapOf<String, File>()
                expected.forEach { path ->
                    val entry = expectedEntries.getValue(path)
                    val destination = File(root, path).canonicalFile
                    corruptIf(
                        destination.path != root.path &&
                            !destination.path.startsWith(root.path + File.separator),
                        "Archive media path escapes staging."
                    )
                    val parent = requireNotNull(destination.parentFile)
                    check(parent.exists() || parent.mkdirs()) {
                        "Restore media directory could not be created."
                    }
                    zip.getInputStream(entry).use { input ->
                        destination.outputStream().buffered().use { output ->
                            input.copyBounded(
                                output = output,
                                limit = MAX_RESTORED_MEDIA_BYTES,
                                message = "Archive media exceeds the safety limit."
                            )
                        }
                    }
                    corruptIf(
                        destination.length() != entry.size ||
                            destination.crc32() != entry.crc,
                        "Archive media integrity check failed."
                    )
                    media[path] = destination
                }
                return StagedPortableArchive(archive, media, stagingDirectory)
            }
        } catch (error: InvalidPortableArchiveException) {
            stagingDirectory.deleteRecursively()
            throw error
        } catch (error: CorruptPortableArchiveException) {
            stagingDirectory.deleteRecursively()
            throw error
        } catch (error: ZipException) {
            stagingDirectory.deleteRecursively()
            throw CorruptPortableArchiveException("Archive ZIP is invalid.", error)
        } catch (error: Exception) {
            stagingDirectory.deleteRecursively()
            throw error
        }
    }

    private fun safeArchivePath(path: String) {
        corruptIf(
            path.isBlank() ||
                path.startsWith("/") ||
                path.startsWith("\\") ||
                path.contains('\\') ||
                path.split('/').any { segment -> segment.isBlank() || segment == ".." },
            "Archive contains an unsafe path."
        )
    }

    private fun InputStream.readBounded(limit: Long, message: String): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        copyBounded(output, limit, message)
        return output.toByteArray()
    }

    private fun InputStream.copyBounded(
        output: java.io.OutputStream,
        limit: Long,
        message: String
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) throw CorruptPortableArchiveException(message)
            output.write(buffer, 0, read)
        }
    }

    private fun corruptIf(condition: Boolean, message: String) {
        if (condition) throw CorruptPortableArchiveException(message)
    }

    private fun File.crc32(): Long {
        val checksum = CRC32()
        inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                checksum.update(buffer, 0, read)
            }
        }
        return checksum.value
    }

    private companion object {
        const val MAX_DATA_BYTES = 20L * 1024L * 1024L
        const val MAX_RESTORED_MEDIA_BYTES = 30L * 1024L * 1024L
        const val MAX_TOTAL_RESTORED_MEDIA_BYTES = 2L * 1024L * 1024L * 1024L
        const val MAX_ARCHIVE_ENTRIES = 200_001
        const val RESTORE_WORKING_SAFETY_BYTES = 64L * 1024L * 1024L
    }
}
