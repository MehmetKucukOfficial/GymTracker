package com.gymtracker.ui.golden

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

fun SemanticsNodeInteraction.assertMatchesGolden(name: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val testContext = instrumentation.context
    val targetContext = instrumentation.targetContext
    val actual = captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
    val assetName = "goldens/$name.png"
    val expected = runCatching {
        testContext.assets.open(assetName).use(BitmapFactory::decodeStream)
    }.getOrNull()

    if (expected == null) {
        val output = File(requireNotNull(targetContext.getExternalFilesDir(null)), assetName)
        output.parentFile?.mkdirs()
        output.outputStream().use { actual.compress(Bitmap.CompressFormat.PNG, 100, it) }
        error("Missing golden $assetName. Recorded candidate at ${output.absolutePath}")
    }

    assertEquals("Golden width changed for $name", expected.width, actual.width)
    assertEquals("Golden height changed for $name", expected.height, actual.height)
    val expectedPixels = IntArray(expected.width * expected.height)
    val actualPixels = IntArray(actual.width * actual.height)
    expected.getPixels(expectedPixels, 0, expected.width, 0, 0, expected.width, expected.height)
    actual.getPixels(actualPixels, 0, actual.width, 0, 0, actual.width, actual.height)
    var changedPixels = 0
    expectedPixels.indices.forEach { index ->
        val first = expectedPixels[index]
        val second = actualPixels[index]
        val changed = abs((first shr 16 and 0xff) - (second shr 16 and 0xff)) > CHANNEL_TOLERANCE ||
            abs((first shr 8 and 0xff) - (second shr 8 and 0xff)) > CHANNEL_TOLERANCE ||
            abs((first and 0xff) - (second and 0xff)) > CHANNEL_TOLERANCE
        if (changed) changedPixels++
    }
    val changedRatio = changedPixels.toFloat() / expectedPixels.size.toFloat()
    assertTrue(
        "Golden $name changed by ${(changedRatio * 100f)}% of pixels",
        changedRatio <= MAX_CHANGED_PIXEL_RATIO
    )
}

private const val CHANNEL_TOLERANCE = 8
private const val MAX_CHANGED_PIXEL_RATIO = 0.005f
