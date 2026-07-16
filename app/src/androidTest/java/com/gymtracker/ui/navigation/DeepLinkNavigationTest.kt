package com.gymtracker.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.MainActivity
import com.gymtracker.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkNavigationTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun manifest_resolvesSupportedExternalUriToMainActivity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("gymtracker://app/progress/photos"))
        val matches = context.packageManager.queryIntentActivities(intent, 0)

        assertTrue(matches.any { it.activityInfo.name == MainActivity::class.java.name })
    }

    @Test
    fun progressPhotosDeepLink_coldStartsOwnedChildAndSurvivesRecreation() {
        launch("progress/photos").use { scenario ->
            composeRule.onNodeWithText(text(R.string.progress_photos_title)).assertIsDisplayed()
            composeRule.onNodeWithTag("bottom-nav-progress").assertIsSelected()

            scenario.recreate()

            composeRule.onNodeWithText(text(R.string.progress_photos_title)).assertIsDisplayed()
            composeRule.onNodeWithTag("bottom-nav-progress").assertIsSelected()
        }
    }

    @Test
    fun programDetailDeepLink_resolvesPositiveIdAndSurvivesRecreation() {
        launch("programs/1").use { scenario ->
            waitForText(R.string.program_plan_title)
            composeRule.onNodeWithText(text(R.string.program_plan_title)).assertIsDisplayed()
            composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()

            scenario.recreate()

            waitForText(R.string.program_plan_title)
            composeRule.onNodeWithText(text(R.string.program_plan_title)).assertIsDisplayed()
            composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()
        }
    }

    @Test
    fun invalidDetailDeepLink_fallsBackToSafeStartDestination() {
        launchUri("gymtracker://app/workouts/0").use {
            composeRule.onNodeWithTag("bottom-nav-home").assertIsSelected()
        }
    }

    private fun launch(route: String): ActivityScenario<MainActivity> {
        return launchUri(DeepLinkContract.uriForRoute(route))
    }

    private fun launchUri(uri: String): ActivityScenario<MainActivity> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(uri)
        }
        return ActivityScenario.launch(intent)
    }

    private fun waitForText(id: Int) {
        val expected = text(id)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
