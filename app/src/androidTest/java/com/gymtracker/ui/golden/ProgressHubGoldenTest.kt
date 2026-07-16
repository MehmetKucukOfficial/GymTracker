package com.gymtracker.ui.golden

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
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
import com.gymtracker.ui.screens.progress.ProgressHubScreen
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.Locale
import org.junit.Rule
import org.junit.Test

class ProgressHubGoldenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightEnglishProgressHubMatchesGolden() {
        render(locale = Locale.US, darkTheme = false, fontScale = 1f)
        composeRule.onNodeWithTag(GOLDEN_ROOT).assertMatchesGolden("progress_hub_light_en")
    }

    @Test
    fun darkTurkishLargeFontProgressHubMatchesGolden() {
        render(locale = Locale("tr", "TR"), darkTheme = true, fontScale = 2f)
        composeRule.onNodeWithTag(GOLDEN_ROOT).assertMatchesGolden("progress_hub_dark_tr_font200")
    }

    private fun render(locale: Locale, darkTheme: Boolean, fontScale: Float) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(locale))
            this.fontScale = fontScale
        }
        val localizedContext = base.createConfigurationContext(configuration)
        val density = Density(base.resources.displayMetrics.density, fontScale)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalDensity provides density
            ) {
                GymTrackerTheme(darkTheme = darkTheme) {
                    Surface {
                        Box(
                            Modifier
                                .requiredSize(width = 360.dp, height = 640.dp)
                                .testTag(GOLDEN_ROOT)
                        ) {
                            ProgressHubScreen(onNavigate = {})
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val GOLDEN_ROOT = "golden-root"
    }
}
