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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutStatus
import com.gymtracker.ui.screens.programs.ProgramsContent
import com.gymtracker.ui.screens.workout.WorkoutListContent
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.Locale
import java.util.TimeZone
import org.junit.Rule
import org.junit.Test

class AdaptiveListDetailGoldenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightEnglishProgramsListDetailMatchesGolden() {
        render(locale = Locale.US, darkTheme = false, fontScale = 1f) {
            ProgramsContent(
                programs = programs(),
                onProgramSelected = {},
                onPurchase = {}
            )
        }
        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("programs_list_detail_light_en")
    }

    @Test
    fun darkTurkishWorkoutListDetailMatchesGolden() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        render(locale = Locale("tr", "TR"), darkTheme = true, fontScale = 1.3f) {
            WorkoutListContent(
                activeSession = workout(11, "Akşam Güç", WorkoutStatus.ACTIVE),
                completedSessions = listOf(
                    workout(7, "Sabah Kondisyon", WorkoutStatus.COMPLETED, 48),
                    workout(5, "Tüm Vücut", WorkoutStatus.COMPLETED, 62)
                ),
                onNewSession = {},
                onOpenSession = {}
            )
        }
        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("workout_list_detail_dark_tr_font130")
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
                                .requiredSize(width = 1_000.dp, height = 700.dp)
                                .testTag(GOLDEN_ROOT)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }

    private fun programs() = listOf(
        Program(
            id = 1,
            name = "Full Body Foundation",
            description = "A balanced strength foundation with three guided training days.",
            durationWeeks = 8,
            daysPerWeek = 3,
            difficulty = ProgramDifficulty.BEGINNER,
            creatorName = "GymTracker"
        ),
        Program(
            id = 2,
            name = "Upper Body Strength",
            description = "Progressive upper-body strength work for intermediate athletes.",
            durationWeeks = 10,
            daysPerWeek = 4,
            difficulty = ProgramDifficulty.INTERMEDIATE,
            isPaid = true,
            price = 149.99f,
            creatorName = "Coach Ada"
        ),
        Program(
            id = 3,
            name = "Athletic Performance",
            description = "Power, conditioning and movement quality in one plan.",
            durationWeeks = 6,
            daysPerWeek = 4,
            difficulty = ProgramDifficulty.ADVANCED,
            creatorName = "GymTracker"
        )
    )

    private fun workout(
        id: Long,
        name: String,
        status: WorkoutStatus,
        durationMinutes: Int = 0
    ) = WorkoutSession(
        id = id,
        name = name,
        date = 1_735_732_800_000,
        durationMinutes = durationMinutes,
        notes = if (status == WorkoutStatus.ACTIVE) "Omuzlarını ısıtmayı unutma." else "Plan tamamlandı.",
        status = status,
        completedAt = if (status == WorkoutStatus.COMPLETED) 1_735_736_400_000 else null
    )

    private companion object {
        const val GOLDEN_ROOT = "adaptive-list-detail-golden-root"
    }
}
