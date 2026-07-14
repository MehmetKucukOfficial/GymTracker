package com.gymtracker.data.photos

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

data class OpenedPhotoSource(
    val inputStream: InputStream,
    val mimeType: String
)

interface PhotoSourceReader {
    fun open(uri: Uri): OpenedPhotoSource
}

@Singleton
class AndroidPhotoSourceReader @Inject constructor(
    @ApplicationContext private val context: Context
) : PhotoSourceReader {

    override fun open(uri: Uri): OpenedPhotoSource {
        val mimeType = context.contentResolver.getType(uri) ?: "image/*"
        require(mimeType.startsWith("image/")) { "Seçilen dosya bir görsel değil." }
        val stream = context.contentResolver.openInputStream(uri)
            ?: error("Seçilen görsel açılamadı.")
        return OpenedPhotoSource(stream, mimeType)
    }
}
