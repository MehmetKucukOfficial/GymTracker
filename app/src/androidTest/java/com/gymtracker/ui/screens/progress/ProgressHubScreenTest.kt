package com.gymtracker.ui.screens.progress

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProgressHubScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun largeFontKeepsAllDestinationsReachableWithMergedButtonSemantics() {
        val route = AtomicReference<String>()
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    ProgressHubScreen(onNavigate = route::set)
                }
            }
        }

        composeRule.onNodeWithTag("progress-destination-progress/load")
            .performScrollTo().assertIsDisplayed().assertHasClickAction().performClick()
        assertEquals("progress/load", route.get())
        composeRule.onNodeWithTag("progress-destination-progress/photos")
            .performScrollTo().assertIsDisplayed()
    }
}
