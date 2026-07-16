plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("ciQuality") {
    group = "verification"
    description = "Runs the deterministic JVM, lint, debug, and minified release quality gate."
    dependsOn(
        ":app:testDebugUnitTest",
        ":app:lintDebug",
        ":app:assembleDebug",
        ":app:assembleRelease",
        ":app:bundleRelease",
        ":app:verifyReleaseProfiles"
    )
}
