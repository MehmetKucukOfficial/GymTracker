package com.gymtracker.ui.screens.progress

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        composeRule.onNodeWithText("Henüz fotoğraf yok").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("İlk fotoğrafı ekle").performScrollTo().performClick()
        assertEquals(1, clicks.get())
    }

    @Test
    fun legacyPhotoHasExplicitDeviceRiskBadge() {
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
                    onDeletePhoto = {}
                )
            }
        }

        composeRule.onNodeWithTag("progress-photo-grid").assertIsDisplayed()
        composeRule.onNodeWithTag("progress-photo-grid")
            .performScrollToNode(hasTestTag("progress-photo-legacy"))
        composeRule.onNodeWithTag("progress-photo-legacy").assertIsDisplayed()
        composeRule.onNodeWithText("Eski kayıt").assertIsDisplayed()
    }
}
