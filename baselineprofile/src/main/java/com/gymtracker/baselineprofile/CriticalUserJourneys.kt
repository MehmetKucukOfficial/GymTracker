package com.gymtracker.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

internal const val PACKAGE_NAME = "com.gymtracker"

internal fun MacrobenchmarkScope.startFromHome() {
    pressHome()
    startActivityAndWait()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.openWorkout() {
    val workout = device.wait(
        Until.findObject(By.text(Pattern.compile("Antrenman|Workouts", Pattern.CASE_INSENSITIVE))),
        5_000
    )
    checkNotNull(workout) { "Workout navigation destination was not visible" }
    val center = workout.visibleCenter
    check(device.click(center.x, center.y)) { "Workout navigation destination could not be tapped" }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.scrollHome() {
    val content = device.findObject(By.scrollable(true)) ?: return
    content.setGestureMargin(device.displayWidth / 5)
    content.fling(Direction.DOWN)
    device.waitForIdle()
    content.fling(Direction.UP)
    device.waitForIdle()
}
