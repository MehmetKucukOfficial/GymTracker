package com.gymtracker.baselineprofile

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.util.regex.Pattern
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance coverage that runs outside the target app process.
 *
 * Activity recreation tests cannot prove that the Linux process was removed. These tests launch a
 * real product route, move its task to the background, kill the target process through the Android
 * activity manager, and bring the existing task back to the foreground. A changed PID plus the
 * restored destination proves that Android recreated the task from its saved navigation state.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ProcessDeathNavigationTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @After
    fun cleanUp() {
        device.executeShellCommand("am task lock stop")
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        device.pressHome()
    }

    @Test
    fun progressPhotosTask_restoresOwnedChildAfterRealProcessDeath() {
        assertTaskRestoredAfterProcessDeath(
            deepLink = "gymtracker://app/progress/photos",
            expectedText = Pattern.compile(
                "İlerleme fotoğrafları|Progress photos",
                Pattern.CASE_INSENSITIVE
            )
        )
    }

    @Test
    fun programDetailTask_restoresPositiveIdAfterRealProcessDeath() {
        assertTaskRestoredAfterProcessDeath(
            deepLink = "gymtracker://app/programs/1",
            expectedText = Pattern.compile(
                "Haftalık antrenman planı|Weekly workout plan",
                Pattern.CASE_INSENSITIVE
            )
        )
    }

    private fun assertTaskRestoredAfterProcessDeath(
        deepLink: String,
        expectedText: Pattern
    ) {
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        val launchOutput = device.executeShellCommand(
            "am start -W --activity-clear-task " +
                "-a android.intent.action.VIEW -d $deepLink $PACKAGE_NAME"
        )
        assertTrue("Deep link launch failed: $launchOutput", launchOutput.contains("Status: ok"))
        assertDestinationVisible(expectedText)

        val originalPid = waitForPid()
        val taskId = currentTaskId()
        device.pressHome()
        device.waitForIdle()

        device.executeShellCommand("am kill $PACKAGE_NAME")
        assertTrue(
            "Target process $originalPid was not killed while its task was backgrounded",
            waitUntil(PROCESS_TIMEOUT_MS) { currentPid().isEmpty() }
        )

        val restoreOutput = device.executeShellCommand("am task lock $taskId")
        assertTrue(
            "Android could not foreground task $taskId: $restoreOutput",
            !restoreOutput.contains("Error", ignoreCase = true)
        )
        val restoredPid = waitForPid(differentFrom = originalPid)
        assertNotEquals("Task restore reused the killed process", originalPid, restoredPid)
        assertDestinationVisible(expectedText)
    }

    private fun assertDestinationVisible(expectedText: Pattern) {
        val destination = device.wait(
            Until.findObject(By.text(expectedText)),
            DESTINATION_TIMEOUT_MS
        )
        assertNotNull("Expected destination text was not visible: $expectedText", destination)
    }

    private fun currentTaskId(): Int {
        val recents = device.executeShellCommand("dumpsys activity recents")
        val match = RECENT_TASK.find(recents)
        assertNotNull("No recent task found for $PACKAGE_NAME\n$recents", match)
        return checkNotNull(match).groupValues[1].toInt()
    }

    private fun waitForPid(differentFrom: String? = null): String {
        var pid = ""
        val found = waitUntil(PROCESS_TIMEOUT_MS) {
            pid = currentPid()
            pid.isNotEmpty() && pid != differentFrom
        }
        assertTrue(
            "No new $PACKAGE_NAME process appeared; previous PID=$differentFrom, current PID=$pid",
            found
        )
        return pid
    }

    private fun currentPid(): String =
        device.executeShellCommand("pidof $PACKAGE_NAME").trim()

    private fun waitUntil(timeoutMs: Long, predicate: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return true
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return predicate()
    }

    private companion object {
        const val DESTINATION_TIMEOUT_MS = 15_000L
        const val PROCESS_TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 200L

        val RECENT_TASK = Regex(
            "Recent #\\d+: Task\\{[^\\r\\n]*#(\\d+)[^\\r\\n]*com\\.gymtracker"
        )
    }
}
