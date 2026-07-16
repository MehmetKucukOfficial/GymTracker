import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val releaseSigningValues = mapOf(
    "GYMTRACKER_UPLOAD_STORE_FILE" to System.getenv("GYMTRACKER_UPLOAD_STORE_FILE"),
    "GYMTRACKER_UPLOAD_STORE_PASSWORD" to System.getenv("GYMTRACKER_UPLOAD_STORE_PASSWORD"),
    "GYMTRACKER_UPLOAD_KEY_ALIAS" to System.getenv("GYMTRACKER_UPLOAD_KEY_ALIAS"),
    "GYMTRACKER_UPLOAD_KEY_PASSWORD" to System.getenv("GYMTRACKER_UPLOAD_KEY_PASSWORD")
)
val missingReleaseSigningValues = releaseSigningValues.filterValues { it.isNullOrBlank() }.keys
val releaseSigningConfigured = missingReleaseSigningValues.isEmpty()

android {
    namespace = "com.gymtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gymtracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("production") {
                storeFile = file(checkNotNull(releaseSigningValues["GYMTRACKER_UPLOAD_STORE_FILE"]))
                storePassword = releaseSigningValues["GYMTRACKER_UPLOAD_STORE_PASSWORD"]
                keyAlias = releaseSigningValues["GYMTRACKER_UPLOAD_KEY_ALIAS"]
                keyPassword = releaseSigningValues["GYMTRACKER_UPLOAD_KEY_PASSWORD"]
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("production")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

baselineProfile {
    saveInSrc = true
    automaticGenerationDuringBuild = false
    filter {
        include("com.gymtracker.**")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coil.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
}

val verifyReleaseSigningConfiguration by tasks.registering {
    group = "verification"
    description = "Fails fast unless every production signing secret and the keystore file are available."
    doLast {
        check(releaseSigningConfigured) {
            "Missing production signing environment values: ${missingReleaseSigningValues.sorted().joinToString()}"
        }
        val storePath = checkNotNull(releaseSigningValues["GYMTRACKER_UPLOAD_STORE_FILE"])
        check(file(storePath).isFile) { "Production keystore does not exist: $storePath" }
    }
}

val verifyReleaseApkSignature by tasks.registering(Exec::class) {
    group = "verification"
    description = "Verifies the production release APK with Android build-tools apksigner."
    dependsOn("assembleRelease", verifyReleaseSigningConfiguration)
    doFirst {
        val executableName = if (System.getProperty("os.name").startsWith("Windows", true)) {
            "apksigner.bat"
        } else {
            "apksigner"
        }
        val apksigner = android.sdkDirectory
            .resolve("build-tools")
            .resolve(android.buildToolsVersion)
            .resolve(executableName)
        val apk = layout.buildDirectory.file("outputs/apk/release/app-release.apk").get().asFile
        check(apksigner.isFile) { "apksigner not found: $apksigner" }
        check(apk.isFile) { "Signed release APK not found: $apk" }
        commandLine(apksigner.absolutePath, "verify", "--verbose", "--print-certs", apk.absolutePath)
    }
}

val verifyReleaseBundleSignature by tasks.registering(Exec::class) {
    group = "verification"
    description = "Verifies the production release AAB JAR signature."
    dependsOn("bundleRelease", verifyReleaseSigningConfiguration)
    doFirst {
        val executableName = if (System.getProperty("os.name").startsWith("Windows", true)) {
            "jarsigner.exe"
        } else {
            "jarsigner"
        }
        val jarsigner = file(System.getProperty("java.home")).resolve("bin").resolve(executableName)
        val bundle = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
        check(jarsigner.isFile) { "jarsigner not found: $jarsigner" }
        check(bundle.isFile) { "Signed release bundle not found: $bundle" }
        commandLine(jarsigner.absolutePath, "-verify", "-certs", bundle.absolutePath)
    }
}

val verifyReleaseProfiles by tasks.registering {
    group = "verification"
    description = "Verifies that release APK and AAB artifacts contain the compiled Baseline Profile."
    dependsOn("assembleRelease", "bundleRelease")
    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/release/${if (releaseSigningConfigured) "app-release.apk" else "app-release-unsigned.apk"}").get().asFile
        val bundle = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
        check(apk.isFile) { "Release APK not found: $apk" }
        check(bundle.isFile) { "Release bundle not found: $bundle" }

        ZipFile(apk).use { zip ->
            check(zip.getEntry("assets/dexopt/baseline.prof") != null) {
                "Release APK does not contain assets/dexopt/baseline.prof"
            }
        }
        ZipFile(bundle).use { zip ->
            check(zip.getEntry("BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof") != null) {
                "Release AAB does not contain the compiled Baseline Profile"
            }
        }
    }
}

tasks.register("releaseDistribution") {
    group = "distribution"
    description = "Builds and cryptographically verifies production-signed APK and AAB artifacts."
    dependsOn(verifyReleaseApkSignature, verifyReleaseBundleSignature, verifyReleaseProfiles)
}
