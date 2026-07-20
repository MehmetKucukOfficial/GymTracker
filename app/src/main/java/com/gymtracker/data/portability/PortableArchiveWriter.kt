package com.gymtracker.data.portability

import com.gymtracker.data.photos.PhotoFileStorage
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun interface PortableMediaSource {
    fun open(uri: String): InputStream?
}

@Singleton
class ManagedPhotoMediaSource @Inject constructor(
    private val storage: PhotoFileStorage
) : PortableMediaSource {
    override fun open(uri: String): InputStream? = storage.openManaged(uri)
}

class MissingManagedPhotoException(
    val photoId: Long,
    val assetPath: String
) : IOException("Managed photo $photoId is missing export asset $assetPath.")

data class ArchiveWriteSummary(
    val includedManagedPhotos: Int,
    val metadataOnlyLegacyPhotos: Int,
    val mediaEntries: Int
)

@Singleton
class PortableArchiveWriter private constructor(
    private val mediaSource: PortableMediaSource
) {
    @Inject
    constructor(mediaSource: ManagedPhotoMediaSource) : this(mediaSource as PortableMediaSource)

    internal constructor(openMedia: (String) -> InputStream?) :
        this(PortableMediaSource(openMedia))

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    fun write(
        archive: PortableArchive,
        snapshot: DatabaseSnapshot,
        output: OutputStream
    ): ArchiveWriteSummary {
        require(archive.format == PORTABLE_ARCHIVE_FORMAT)
        require(archive.formatVersion == PORTABLE_ARCHIVE_VERSION)
        require(archive.databaseSchemaVersion == PORTABLE_DATABASE_SCHEMA_VERSION)

        val portablePhotos = archive.data.progressPhotos.associateBy { photo -> photo.id }
        var mediaEntries = 0
        val zip = ZipOutputStream(NonClosingOutputStream(output))
        try {
            zip.putNextEntry(archiveEntry(PORTABLE_DATA_ENTRY, archive.exportedAtEpochMillis))
            zip.write(json.encodeToString(archive).encodeToByteArray())
            zip.closeEntry()

            snapshot.progressPhotos
                .filter { photo -> photo.storageVersion >= 1 }
                .forEach { photo ->
                    val portable = requireNotNull(portablePhotos[photo.id]) {
                        "Portable photo metadata is missing for ID ${photo.id}."
                    }
                    writeMedia(
                        zip = zip,
                        photoId = photo.id,
                        uri = photo.photoUri,
                        assetPath = requireNotNull(portable.mainAssetPath),
                        exportedAtEpochMillis = archive.exportedAtEpochMillis
                    )
                    mediaEntries++
                    if (portable.thumbnailAssetPath != null) {
                        writeMedia(
                            zip = zip,
                            photoId = photo.id,
                            uri = requireNotNull(photo.thumbnailUri),
                            assetPath = portable.thumbnailAssetPath,
                            exportedAtEpochMillis = archive.exportedAtEpochMillis
                        )
                        mediaEntries++
                    }
                }
            zip.finish()
            zip.flush()
        } finally {
            runCatching { zip.close() }
        }

        return ArchiveWriteSummary(
            includedManagedPhotos = archive.data.progressPhotos.count { photo ->
                photo.mediaState == PortablePhotoMediaState.INCLUDED
            },
            metadataOnlyLegacyPhotos = archive.data.progressPhotos.count { photo ->
                photo.mediaState == PortablePhotoMediaState.LEGACY_METADATA_ONLY
            },
            mediaEntries = mediaEntries
        )
    }

    private fun writeMedia(
        zip: ZipOutputStream,
        photoId: Long,
        uri: String,
        assetPath: String,
        exportedAtEpochMillis: Long
    ) {
        val input = mediaSource.open(uri)
            ?: throw MissingManagedPhotoException(photoId, assetPath)
        zip.putNextEntry(archiveEntry(assetPath, exportedAtEpochMillis))
        input.use { source -> copyBounded(source, zip) }
        zip.closeEntry()
    }

    private fun copyBounded(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= MAX_EXPORTED_MEDIA_BYTES) {
                "Managed photo exceeds the export safety limit."
            }
            output.write(buffer, 0, read)
        }
    }

    private fun archiveEntry(path: String, timestamp: Long): ZipEntry {
        require(path.isNotBlank() && !path.startsWith("/") && ".." !in path.split('/')) {
            "Unsafe archive entry path: $path"
        }
        return ZipEntry(path).apply { time = timestamp }
    }

    private class NonClosingOutputStream(output: OutputStream) : FilterOutputStream(output) {
        override fun close() {
            flush()
        }
    }

    private companion object {
        const val MAX_EXPORTED_MEDIA_BYTES = 30L * 1024L * 1024L
    }
}
