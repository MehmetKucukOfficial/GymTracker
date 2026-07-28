package com.gymtracker.ui.screens.progress

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressPhotoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyStateAtTwoHundredPercentFontScaleKeepsPrimaryActionReachable() {
        val clicks = AtomicInteger(0)
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    ProgressPhotoContent(
                        photos = emptyList(),
                        uiState = ProgressPhotoUiState(),
                        onAddPhoto = { clicks.incrementAndGet() },
                        onDeletePhoto = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText(text(R.string.progress_photos_empty_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.progress_photos_first_action)).performScrollTo().performClick()
        assertEquals(1, clicks.get())
    }

    @Test
    fun savingStateAnnouncesProgressAndBlocksConflictingActions() {
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                ProgressPhotoContent(
                    photos = listOf(
                        ProgressPhoto(
                            id = 1,
                            date = 1_000,
                            photoUri = "content://legacy/photo",
                            angle = PhotoAngle.FRONT,
                            storageVersion = 0
                        )
                    ),
                    uiState = ProgressPhotoUiState(isSaving = true),
                    onAddPhoto = {},
                    onDeletePhoto = {}
                )
            }
        }

        composeRule.onNodeWithTag("progress-photo-saving").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.progress_photos_saving)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.progress_photos_first_action)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(
            text(R.string.progress_photos_delete_description, formattedDate(1_000))
        ).assertIsNotEnabled()
        composeRule.onNodeWithTag("progress-photo-upgrade-1").assertIsNotEnabled()
    }

    @Test
    fun legacyPhotoHasExplicitDeviceRiskBadge() {
        val reselectClicks = AtomicInteger(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                ProgressPhotoContent(
                    photos = listOf(
                        ProgressPhoto(
                            id = 1,
                            date = 1_000,
                            photoUri = "content://legacy/photo",
                            angle = PhotoAngle.FRONT,
                            notes = "Başlangıç",
                            storageVersion = 0
                        )
                    ),
                    uiState = ProgressPhotoUiState(),
                    onAddPhoto = {},
                    onDeletePhoto = {},
                    onReselectPhoto = { reselectClicks.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("progress-photo-grid").assertIsDisplayed()
        composeRule.onNodeWithTag("progress-photo-grid")
            .performScrollToNode(hasTestTag("progress-photo-legacy"))
        composeRule.onNodeWithTag("progress-photo-legacy").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.progress_photos_legacy_badge)).assertIsDisplayed()
        composeRule.onNodeWithTag("progress-photo-upgrade-1")
            .assertIsDisplayed()
            .performClick()
        assertEquals(1, reselectClicks.get())
    }

    @Test
    fun v1PhotoOffersOptionalOptimizationAtLargeFontInDarkTheme() {
        val optimizeClicks = AtomicInteger(0)
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    ProgressPhotoContent(
                        photos = listOf(
                            ProgressPhoto(
                                id = 2,
                                date = 2_000,
                                photoUri = "file:///legacy-v1.jpg",
                                angle = PhotoAngle.BACK,
                                storageVersion = 1
                            )
                        ),
                        uiState = ProgressPhotoUiState(),
                        onAddPhoto = {},
                        onDeletePhoto = {},
                        onReoptimizePhoto = { optimizeClicks.incrementAndGet() }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("progress-photo-grid")
            .performScrollToNode(hasTestTag("progress-photo-v1"))
        composeRule.onNodeWithTag("progress-photo-v1").assertIsDisplayed()
        composeRule.onNodeWithTag("progress-photo-upgrade-2")
            .assertIsDisplayed()
            .performClick()
        assertEquals(1, optimizeClicks.get())
    }

    @Test
    fun largeManagedDatasetRemainsLazyAndCanReachLastPhoto() {
        val photos = (1L..500L).map { id ->
            ProgressPhoto(
                id = id,
                date = id,
                photoUri = "file:///missing-main-$id.jpg",
                thumbnailUri = "file:///missing-thumb-$id.jpg",
                angle = PhotoAngle.values()[(id % PhotoAngle.values().size).toInt()],
                storageVersion = 2
            )
        }
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                ProgressPhotoContent(
                    photos = photos,
                    uiState = ProgressPhotoUiState(),
                    onAddPhoto = {},
                    onDeletePhoto = {}
                )
            }
        }

        composeRule.onNodeWithTag("progress-photo-item-500").assertDoesNotExist()
        composeRule.onNodeWithTag("progress-photo-grid")
            .performScrollToNode(hasTestTag("progress-photo-item-500"))
        composeRule.onNodeWithTag("progress-photo-item-500").assertIsDisplayed()
    }

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun text(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private fun formattedDate(epochMillis: Long): String =
        java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM)
            .format(java.util.Date(epochMillis))
}
