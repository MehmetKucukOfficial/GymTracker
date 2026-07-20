package com.gymtracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GtFeedbackComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topAppBarExposesOneLocalizedBackAction() {
        val clicks = AtomicInteger(0)
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                GymTrackerTheme {
                    GtTopAppBar(
                        title = "Ayrıntılı ekran başlığı",
                        supportingText = "Büyük yazıda iki satıra güvenle yayılır",
                        onBack = { clicks.incrementAndGet() }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("gt-top-app-bar").assertIsDisplayed()
        composeRule.onNodeWithText("Ayrıntılı ekran başlığı").assertIsDisplayed()
        composeRule.onNodeWithText("Büyük yazıda iki satıra güvenle yayılır").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.action_back))
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        assertEquals(1, clicks.get())
    }

    @Test
    fun snackbarHostExposesMessageAndUndoAction() {
        composeRule.setContent {
            GymTrackerTheme {
                val hostState = remember { SnackbarHostState() }
                Column {
                    GtSnackbarHost(hostState)
                }
                LaunchedEffect(hostState) {
                    hostState.showSnackbar(
                        message = "Kayıt silindi",
                        actionLabel = "Geri al"
                    )
                }
            }
        }

        composeRule.onNodeWithTag("gt-snackbar-host").assertIsDisplayed()
        composeRule.onNodeWithText("Kayıt silindi").assertIsDisplayed()
        composeRule.onNodeWithText("Geri al").assertIsDisplayed().assertHasClickAction()
    }

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
