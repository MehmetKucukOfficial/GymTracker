package com.gymtracker.ui.screens.workout

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
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
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicLong
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun workoutList_distinguishesActiveSessionFromCompletedHistory() {
        val openedSessionId = AtomicLong(0)

        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f)
            ) {
                GymTrackerTheme(darkTheme = true) {
                    WorkoutListContent(
                        activeSession = session(
                            id = 11,
                            name = "Akşam Güç",
                            status = WorkoutStatus.ACTIVE
                        ),
                        completedSessions = listOf(
                            session(
                                id = 7,
                                name = "Sabah Kondisyon",
                                status = WorkoutStatus.COMPLETED,
                                completedAt = 2_000,
                                durationMinutes = 42
                            )
                        ),
                        onNewSession = {},
                        onOpenSession = openedSessionId::set
                    )
                }
            }
        }

        composeRule.onNodeWithTag("workout-active-card").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("workout-history-section").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Sabah Kondisyon").performScrollTo().assertIsDisplayed()
        assertEquals(11, openedSessionId.get())
    }

    @Test
    fun expandedList_selectsHistoryThenOpensStableIdRouteFromPreview() {
        val openedSessionId = AtomicLong(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                WorkoutListContent(
                    activeSession = session(11, "Active session", WorkoutStatus.ACTIVE),
                    completedSessions = listOf(
                        session(7, "Completed session", WorkoutStatus.COMPLETED, completedAt = 2_000)
                    ),
                    onNewSession = {},
                    onOpenSession = openedSessionId::set,
                    modifier = androidx.compose.ui.Modifier.requiredSize(1_000.dp, 700.dp)
                )
            }
        }

        composeRule.onNodeWithTag("list-detail-layout").assertIsDisplayed()
        composeRule.onNodeWithTag("workout-session-card-7").performClick().assertIsSelected()
        assertEquals(0L, openedSessionId.get())
        composeRule.onNodeWithTag("workout-open-detail").performClick()
        assertEquals(7L, openedSessionId.get())
    }

    @Test
    fun completedWorkout_isReadOnlyAndDoesNotExposeMutationActions() {
        composeRule.setContent {
            GymTrackerTheme(darkTheme = true) {
                WorkoutDetailContent(
                    session = session(
                        id = 7,
                        name = "Tamamlanan Seans",
                        status = WorkoutStatus.COMPLETED,
                        completedAt = 4_000,
                        durationMinutes = 55
                    ),
                    sets = listOf(workoutSet(sessionId = 7)),
                    onBack = {},
                    onFinishRequest = {},
                    onAddSetRequest = {},
                    onDeleteSet = {}
                )
            }
        }

        composeRule.onNodeWithTag("workout-detail-read-only").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.workout_detail_summary)).assertIsDisplayed()
        composeRule.onNodeWithTag("workout-finish-action").assertDoesNotExist()
        composeRule.onNodeWithTag("workout-add-set-action").assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.action_delete)).assertDoesNotExist()
    }

    @Test
    fun activeWorkout_atTwoHundredPercentFontScaleKeepsPrimaryActionsReachable() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f)
            ) {
                GymTrackerTheme(darkTheme = false) {
                    WorkoutDetailContent(
                        session = session(
                            id = 11,
                            name = "Büyük Yazı Seansı",
                            status = WorkoutStatus.ACTIVE
                        ),
                        sets = listOf(workoutSet(sessionId = 11)),
                        onBack = {},
                        onFinishRequest = {},
                        onAddSetRequest = {},
                        onDeleteSet = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("workout-detail-active").assertIsDisplayed()
        composeRule.onNodeWithTag("workout-finish-action").assertIsDisplayed()
        composeRule.onNodeWithTag("workout-add-set-action").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_delete)).assertIsDisplayed()
    }

    @Test
    fun plannedSet_disablesFinishAndExposesCompletionAction() {
        val completedSetId = AtomicLong(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                WorkoutDetailContent(
                    session = session(id = 12, name = "Program Günü", status = WorkoutStatus.ACTIVE),
                    sets = listOf(
                        workoutSet(sessionId = 12).copy(
                            id = 44,
                            reps = 0,
                            weightKg = 0f,
                            isCompleted = false,
                            targetReps = "8-10"
                        )
                    ),
                    onBack = {},
                    onFinishRequest = {},
                    onAddSetRequest = {},
                    onDeleteSet = {},
                    onCompletePlannedSet = { completedSetId.set(it.id) }
                )
            }
        }

        composeRule.onNodeWithTag("workout-finish-action").assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.workout_complete_planned_set)).performClick()
        assertEquals(44L, completedSetId.get())
    }

    private fun session(
        id: Long,
        name: String,
        status: WorkoutStatus,
        completedAt: Long? = null,
        durationMinutes: Int = 0
    ) = WorkoutSession(
        id = id,
        name = name,
        date = 1_000,
        durationMinutes = durationMinutes,
        status = status,
        completedAt = completedAt
    )

    private fun workoutSet(sessionId: Long) = WorkoutSet(
        id = 3,
        sessionId = sessionId,
        exerciseName = "Bench Press",
        setNumber = 1,
        reps = 8,
        weightKg = 60f
    )

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
