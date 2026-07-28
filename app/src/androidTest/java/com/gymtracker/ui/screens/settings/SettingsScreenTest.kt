package com.gymtracker.ui.screens.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.settings.AppSettings
import com.gymtracker.data.settings.ThemeMode
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun themeSelection_exposesSingleSelectionAndEmitsChoice() {
        val selected = AtomicReference<ThemeMode>()
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(themeMode = ThemeMode.SYSTEM),
                    versionName = "1.0",
                    onThemeModeSelected = selected::set,
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings-theme-system").assertIsSelected()
        composeRule.onNodeWithTag("settings-theme-dark").performClick()
        assertEquals(ThemeMode.DARK, selected.get())
    }

    @Test
    fun largeFont_keepsPrivacyAndVersionInformationReachable() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    SettingsContent(
                        settings = AppSettings(themeMode = ThemeMode.DARK),
                        versionName = "1.0",
                        onThemeModeSelected = {},
                        onBack = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("settings-local-first")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings-data-export")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings-delete-data")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings-version")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun dataExportAction_emitsChoice() {
        val exports = AtomicInteger()
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    dataExportState = DataExportState.Idle,
                    onExportData = { exports.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("settings-export-action")
            .performScrollTo()
            .performClick()
        assertEquals(1, exports.get())
    }

    @Test
    fun dataExportAction_isDisabledWhilePreparing() {
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    dataExportState = DataExportState.Exporting,
                    onExportData = {}
                )
            }
        }
        composeRule.onNodeWithTag("settings-export-action")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun dataRestoreAction_emitsChoice() {
        val restores = AtomicInteger()
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    onChooseRestore = { restores.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("settings-restore-action")
            .performScrollTo()
            .performClick()
        assertEquals(1, restores.get())
    }

    @Test
    fun dataActions_areDisabledWhileRestoring() {
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    dataRestoreState = DataRestoreState.Restoring
                )
            }
        }

        composeRule.onNodeWithTag("settings-export-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-restore-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-delete-action")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun deleteAction_emitsConfirmationRequest() {
        val deletions = AtomicInteger()
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    onRequestDelete = { deletions.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("settings-delete-action")
            .performScrollTo()
            .performClick()
        assertEquals(1, deletions.get())
    }

    @Test
    fun dataActions_areDisabledWhileDeleting() {
        composeRule.setContent {
            GymTrackerTheme {
                SettingsContent(
                    settings = AppSettings(),
                    versionName = "1.0",
                    onThemeModeSelected = {},
                    onBack = {},
                    dataDeletionState = DataDeletionState.Deleting
                )
            }
        }

        composeRule.onNodeWithTag("settings-export-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-restore-action")
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-delete-action")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun deleteConfirmation_requiresExactLocalizedPhrase() {
        val confirmations = AtomicInteger()
        val phrase = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(R.string.settings_delete_confirmation_phrase)
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            GymTrackerTheme {
                DeleteConfirmationDialog(
                    value = value,
                    onValueChange = { value = it },
                    onConfirm = { confirmations.incrementAndGet() },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings-delete-confirm-action").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-delete-confirmation").performTextInput(phrase)
        composeRule.onNodeWithTag("settings-delete-confirm-action")
            .assertIsEnabled()
            .performClick()
        assertEquals(1, confirmations.get())
    }
}
