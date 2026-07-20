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
    fun allPrimaryFeaturesHaveEnglishCoverage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList(Locale.US))
        }
        val english = context.createConfigurationContext(configuration)

        assertEquals("Overview", english.getString(R.string.nav_home))
        assertEquals("Weekly overview", english.getString(R.string.home_section_metrics))
        assertEquals("Workouts", english.getString(R.string.workout_title))
        assertEquals("Nutrition", english.getString(R.string.nutrition_title))
        assertEquals("Breakfast", english.getString(R.string.nutrition_meal_breakfast))
        assertEquals("Body measurements", english.getString(R.string.measurements_title))
        assertEquals("Weight trend", english.getString(R.string.measurements_chart_title))
        assertEquals("Progress", english.getString(R.string.progress_hub_title))
        assertEquals("Progress photos", english.getString(R.string.progress_photos_title))
        assertEquals("7-day muscle load", english.getString(R.string.muscle_load_title))
        assertEquals("Mid chest", english.getString(R.string.muscle_chest_mid))
        assertEquals("Programs", english.getString(R.string.programs_title))
        assertEquals("Settings", english.getString(R.string.settings_title))
        assertEquals("Undo", english.getString(R.string.action_undo))
    }
}
