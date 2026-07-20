package com.gymtracker.data.settings

import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun themeMode_resolvesSystemAndExplicitChoices() {
        assertTrue(ThemeMode.SYSTEM.useDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.SYSTEM.useDarkTheme(systemInDarkTheme = false))
        assertFalse(ThemeMode.LIGHT.useDarkTheme(systemInDarkTheme = true))
        assertTrue(ThemeMode.DARK.useDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun missingOrUnknownStoredTheme_fallsBackToSystem() {
        assertEquals(
            ThemeMode.SYSTEM,
            SettingsRepository.preferencesToSettings(mutablePreferencesOf()).themeMode
        )
        assertEquals(
            ThemeMode.SYSTEM,
            SettingsRepository.preferencesToSettings(
                mutablePreferencesOf(SettingsRepository.THEME_MODE to "REMOVED_VALUE")
            ).themeMode
        )
    }

    @Test
    fun storedTheme_isRestoredByStableEnumName() {
        assertEquals(
            ThemeMode.DARK,
            SettingsRepository.preferencesToSettings(
                mutablePreferencesOf(SettingsRepository.THEME_MODE to ThemeMode.DARK.name)
            ).themeMode
        )
    }
}
