package com.gymtracker.ui.screens.programs

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.programs.ProgramWorkoutPlan
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicLong
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun listAtTwoHundredPercentFontScale_keepsProgramReachable() {
        val selected = AtomicLong(0)
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    ProgramsContent(
                        programs = listOf(program()),
                        onProgramSelected = selected::set,
                        onPurchase = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("Full Body 3 Gün").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1L, selected.get())
    }

    @Test
    fun expandedList_selectsLocallyThenOpensStableIdRouteFromPreview() {
        val selected = AtomicLong(0)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            GymTrackerTheme(darkTheme = false) {
                ProgramsContent(
                    programs = listOf(
                        program(),
                        program().copy(id = 2, name = "Upper body strength")
                    ),
                    onProgramSelected = selected::set,
                    onPurchase = {},
                    modifier = androidx.compose.ui.Modifier.requiredSize(1_000.dp, 700.dp)
                )
            }
        }

        composeRule.onNodeWithTag("list-detail-layout").assertIsDisplayed()
        composeRule.onNodeWithTag("program-card-2").performClick().assertIsSelected()
        assertEquals(0L, selected.get())
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithTag("program-card-2").assertIsSelected()
        composeRule.onNodeWithTag("program-open-detail").performClick()
        assertEquals(2L, selected.get())
    }

    @Test
    fun unlockedDetail_exposesRelationalPlanAndStartAction() {
        val started = AtomicLong(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                ProgramDetailContent(
                    program = program(),
                    workouts = listOf(plan()),
                    isStarting = false,
                    onBack = {},
                    onPurchase = {},
                    onStartWorkout = started::set
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.program_start_workout)).performScrollTo().performClick()
        assertEquals(10L, started.get())
    }

    @Test
    fun lockedDetail_hidesPlanAndExplainsDemoAccess() {
        composeRule.setContent {
            GymTrackerTheme(darkTheme = true) {
                ProgramDetailContent(
                    program = program(isPaid = true),
                    workouts = listOf(plan()),
                    isStarting = false,
                    onBack = {},
                    onPurchase = {},
                    onStartWorkout = {}
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.program_locked_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.program_start_workout)).assertDoesNotExist()
    }

    private fun program(isPaid: Boolean = false) = Program(
        id = 1,
        name = "Full Body 3 Gün",
        description = "Dengeli başlangıç programı",
        durationWeeks = 8,
        daysPerWeek = 3,
        difficulty = ProgramDifficulty.BEGINNER,
        isPaid = isPaid,
        price = 99.99f
    )

    private fun plan() = ProgramWorkoutPlan(
        workout = ProgramWorkout(
            id = 10,
            programId = 1,
            weekNumber = 1,
            dayNumber = 1,
            dayName = "Tam Vücut A"
        ),
        exercises = listOf(
            ProgramDayExercise(
                id = 20,
                programWorkoutId = 10,
                exerciseId = "exercise.squat",
                exerciseName = "Squat",
                position = 0,
                targetSets = 3,
                targetReps = "8-10",
                restSeconds = 120
            )
        )
    )

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
