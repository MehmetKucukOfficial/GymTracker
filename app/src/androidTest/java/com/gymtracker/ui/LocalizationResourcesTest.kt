package com.gymtracker.ui

import android.os.LocaleList
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalizationResourcesTest {
    @Test
    fun nutritionAndMeasurementsHaveEnglishCoverage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList(Locale.US))
        }
        val english = context.createConfigurationContext(configuration)

        assertEquals("Nutrition", english.getString(R.string.nutrition_title))
        assertEquals("Breakfast", english.getString(R.string.nutrition_meal_breakfast))
        assertEquals("Body measurements", english.getString(R.string.measurements_title))
        assertEquals("Weight trend", english.getString(R.string.measurements_chart_title))
        assertEquals("Undo", english.getString(R.string.action_undo))
    }
}
