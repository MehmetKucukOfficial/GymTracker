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

    @Test
    fun nutritionTask_restoresSelectedDateAfterRealProcessDeath() {
        var selectedDate = ""
        assertTaskRestoredAfterProcessDeath(
            deepLink = "gymtracker://app/nutrition",
            expectedText = Pattern.compile("Beslenme|Nutrition", Pattern.CASE_INSENSITIVE),
            beforeKill = {
                clickSemanticChild(
                    By.desc(
                        Pattern.compile(
                            "Önceki gün|Previous day",
                            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                        )
                    )
                )
                selectedDate = requireNotNull(
                    device.wait(
                        Until.findObject(By.text(DATE_HEADING)),
                        DESTINATION_TIMEOUT_MS
                    )
                ).text
                assertTrue("Previous-day selection did not expose a date", selectedDate.isNotBlank())
            },
            afterRestore = {
                val restoredDate = device.wait(
                    Until.findObject(By.text(selectedDate)),
                    DESTINATION_TIMEOUT_MS
                )
                assertNotNull("Selected nutrition date was not restored: $selectedDate", restoredDate)
            }
        )
    }

    @Test
    fun measurementsTask_restoresChartPeriodAfterRealProcessDeath() {
        assertTaskRestoredAfterProcessDeath(
            deepLink = "gymtracker://app/progress/measurements",
            expectedText = Pattern.compile(
                "Vücut ölçümleri|Body measurements",
                Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
            ),
            beforeKill = {
                ensureMeasurementExists()
                val period = requireNotNull(
                    device.wait(
                        Until.findObject(By.text(NINETY_DAY_LABEL)),
                        DESTINATION_TIMEOUT_MS
                    )
                )
                period.parent.click()
                assertTrue("90-day chart period was not selected", period.parent.isChecked)
            },
            afterRestore = {
                val restoredPeriod = device.wait(
                    Until.findObject(By.text(NINETY_DAY_LABEL)),
                    DESTINATION_TIMEOUT_MS
                )
                assertNotNull("90-day chart period was not visible after restore", restoredPeriod)
                assertTrue(
                    "90-day chart period lost its checked state after process death",
                    requireNotNull(restoredPeriod).parent.isChecked
                )
            }
        )
    }

    private fun assertTaskRestoredAfterProcessDeath(
        deepLink: String,
        expectedText: Pattern,
        beforeKill: () -> Unit = {},
        afterRestore: () -> Unit = {}
    ) {
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        val launchOutput = device.executeShellCommand(
            "am start -W --activity-clear-task " +
                "-a android.intent.action.VIEW -d $deepLink $PACKAGE_NAME"
        )
        assertTrue("Deep link launch failed: $launchOutput", launchOutput.contains("Status: ok"))
        assertDestinationVisible(expectedText)
        beforeKill()

        val originalPid = waitForPid()
        val taskId = currentTaskId()
        device.pressHome()
        device.waitForIdle()

        // Android protects the immediately previous app at a stronger OOM adjustment. Opening a
        // second system task moves the target from "previous" to a cached/background process, so
        // `am kill` has deterministic platform semantics on both local and hosted emulators.
        val settingsOutput = device.executeShellCommand(
            "am start -W -a android.settings.SETTINGS"
        )
        assertTrue(
            "System Settings could not be opened to background the target task: $settingsOutput",
            settingsOutput.contains("Status: ok")
        )
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
        afterRestore()
    }

    private fun ensureMeasurementExists() {
        if (device.hasObject(By.text(NINETY_DAY_LABEL))) return

        clickSemanticChild(By.text(ADD_MEASUREMENT_LABEL))
        val fields = device.wait(
            Until.findObjects(By.clazz("android.widget.EditText")),
            DESTINATION_TIMEOUT_MS
        )
        assertTrue("Measurement dialog did not expose weight input", fields.size >= 2)
        fields[1].text = "80"
        dismissSoftKeyboardIfVisible()
        clickSemanticChild(By.text(SAVE_LABEL))
        assertNotNull(
            "Chart period controls did not appear after creating a measurement",
            device.wait(Until.findObject(By.text(NINETY_DAY_LABEL)), DESTINATION_TIMEOUT_MS)
        )
    }

    private fun clickSemanticChild(selector: androidx.test.uiautomator.BySelector) {
        val child = device.wait(Until.findObject(selector), DESTINATION_TIMEOUT_MS)
        assertNotNull("Expected semantic action was not visible: $selector", child)
        val action = requireNotNull(child)
        val clickable = generateSequence(action) { it.parent }.firstOrNull { it.isClickable }
        assertNotNull("No clickable ancestor was found for: $selector", clickable)
        requireNotNull(clickable).click()
        device.waitForIdle()
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

    private fun dismissSoftKeyboardIfVisible() {
        val inputMethodState = device.executeShellCommand("dumpsys input_method")
        if (!inputMethodState.contains("mInputShown=true")) return

        device.pressBack()
        assertTrue(
            "Soft keyboard did not close without dismissing the measurement dialog",
            waitUntil(PROCESS_TIMEOUT_MS) {
                !device.executeShellCommand("dumpsys input_method")
                    .contains("mInputShown=true")
            }
        )
    }

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

        val DATE_HEADING = Pattern.compile(
            "\\d{1,2}\\s+\\p{L}+\\s+\\d{4},\\s+\\p{L}+",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
        val NINETY_DAY_LABEL = Pattern.compile(
            "90 gün|90 days",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
        val ADD_MEASUREMENT_LABEL = Pattern.compile(
            "İlk ölçümü ekle|Ölçüm ekle|Add first measurement|Add measurement",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
        val SAVE_LABEL = Pattern.compile(
            "Kaydet|Save",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )

        val RECENT_TASK = Regex(
            "Recent #\\d+: Task\\{[^\\r\\n]*#(\\d+)[^\\r\\n]*com\\.gymtracker"
        )
    }
}
