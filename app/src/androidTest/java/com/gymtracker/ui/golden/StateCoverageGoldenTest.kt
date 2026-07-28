package com.gymtracker.ui.golden

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.settings.AppSettings
import com.gymtracker.data.settings.ThemeMode
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.domain.MuscleLoad
import com.gymtracker.domain.MuscleLoadLevel
import com.gymtracker.domain.MuscleLoadSummary
import com.gymtracker.ui.screens.fatigue.MuscleLoadContent
import com.gymtracker.ui.screens.fatigue.MuscleLoadUiState
import com.gymtracker.ui.screens.progress.ProgressPhotoContent
import com.gymtracker.ui.screens.progress.ProgressPhotoUiState
import com.gymtracker.ui.screens.settings.DataRestoreState
import com.gymtracker.ui.screens.settings.SettingsContent
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.Locale
import java.util.TimeZone
import org.junit.Rule
import org.junit.Test

class StateCoverageGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun darkTurkishLargeFontPhotosSavingStateMatchesGolden() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        render(locale = Locale("tr", "TR"), darkTheme = true, fontScale = 2f) {
            ProgressPhotoContent(
                photos = listOf(
                    ProgressPhoto(
                        id = 1,
                        date = 1_752_537_600_000,
                        photoUri = "content://legacy/photo",
                        angle = PhotoAngle.FRONT,
                        notes = "Başlangıç pozu",
                        storageVersion = 0
                    )
                ),
                uiState = ProgressPhotoUiState(isSaving = true),
                onAddPhoto = {},
                onDeletePhoto = {}
            )
        }

        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("photos_saving_dark_tr_font200")
    }

    @Test
    fun lightEnglishMuscleLoadContentMatchesGolden() {
        render(locale = Locale.US, darkTheme = false, fontScale = 1f) {
            MuscleLoadContent(
                uiState = MuscleLoadUiState.Content(
                    MuscleLoadSummary(
                        muscles = listOf(
                            MuscleLoad(
                                muscle = MuscleGroup.CHEST_MID,
                                loadScore = 0.72f,
                                weightedVolume = 4_800f,
                                level = MuscleLoadLevel.HIGH
                            ),
                            MuscleLoad(
                                muscle = MuscleGroup.QUADS,
                                loadScore = 0.44f,
                                weightedVolume = 3_200f,
                                level = MuscleLoadLevel.MEDIUM
                            )
                        ),
                        completedSetCount = 8,
                        unknownExerciseCount = 1,
                        bodyweightCalibrationKg = 82.5f,
                        usesPersonalBodyweight = true
                    )
                ),
                onRetry = {}
            )
        }

        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("muscle_load_content_light_en")
    }

    @Test
    fun darkTurkishLargeFontMuscleLoadLoadingMatchesGolden() {
        render(locale = Locale("tr", "TR"), darkTheme = true, fontScale = 2f) {
            MuscleLoadContent(
                uiState = MuscleLoadUiState.Loading,
                onRetry = {}
            )
        }

        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("muscle_load_loading_dark_tr_font200")
    }

    @Test
    fun lightEnglishSettingsRestoreLockMatchesGolden() {
        render(locale = Locale.US, darkTheme = false, fontScale = 1f) {
            SettingsContent(
                settings = AppSettings(themeMode = ThemeMode.SYSTEM),
                versionName = "1.0",
                onThemeModeSelected = {},
                onBack = {},
                dataRestoreState = DataRestoreState.Restoring
            )
        }

        composeRule.onNodeWithTag("settings-restore-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-export-action").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-delete-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("settings_restore_lock_light_en")
    }

    private fun render(
        locale: Locale,
        darkTheme: Boolean,
        fontScale: Float,
        content: @Composable () -> Unit
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(locale))
            this.fontScale = fontScale
        }
        val localizedContext = base.createConfigurationContext(configuration)
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalDensity provides Density(density = 1f, fontScale = fontScale)
            ) {
                GymTrackerTheme(darkTheme = darkTheme) {
                    Surface {
                        Box(
                            Modifier
                                .requiredSize(width = 360.dp, height = 800.dp)
                                .testTag(GOLDEN_ROOT)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(500)
    }

    private companion object {
        const val GOLDEN_ROOT = "state-coverage-golden-root"
    }
}
