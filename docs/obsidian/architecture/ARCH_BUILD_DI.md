---
type: architecture
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-15
source_files:
  - build.gradle.kts
  - app/build.gradle.kts
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.properties
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/gymtracker/di/DatabaseModule.kt
tags: [gymtracker/architecture, gymtracker/build, gymtracker/quality]
---

# Mimari — Build, DI ve Platform

## Toolchain

> Update (2026-07-14): Gradle 8.4 wrapper scripts/JAR and `app/proguard-rules.pro` are now present. The manifest no longer declares `CAMERA` or `READ_MEDIA_IMAGES`; the app uses the system `GetContent()` picker instead. Five unused temporary bitmap resources were removed. `lintDebug` has zero errors and 40 remaining warnings. The persistent URI problem remains tracked separately in [[FEATURE_PROGRESS_PHOTOS]].

- AGP 8.3.1
- Kotlin 1.9.24
- Compose BOM 2024.06.00
- Compose compiler 1.5.14
- Gradle 8.4 wrapper script/JAR kaynakta mevcut
- JDK 17 gerekir; makine varsayılanı Java 11
- compile/target SDK 34, min SDK 26

## DI

`GymTrackerApp` → `@HiltAndroidApp`; `MainActivity` → `@AndroidEntryPoint`; `DatabaseModule` singleton DB ve 5 DAO sağlar. Home, workout, program ve photo repository sınırları constructor injection ile aktiftir; `PhotoDataModule` source-reader interface binding’ini sağlar.

## Build doğrulaması

- `assembleDebug`: başarılı
- `testDebugUnitTest`: 8/8 başarılı
- `connectedDebugAndroidTest`: 15/15 başarılı, Pixel 6 Pro API 34
- `lintDebug`: 0 error, 52 warnings

## Manifest riskleri

- Kamera/medya izni yok; sistem picker seçili URI için geçici erişim verir ve uygulama internal kopya alır.
- `allowBackup=true`, özel backup kuralı yok.

## İlk işler

1. CI kalite zinciri
2. Release/R8 doğrulaması
3. Backup/extraction politikası
4. Kontrollü dependency güncellemeleri

Yol haritası: [[ROADMAP_EXECUTION_GRAPH#PR 1 — Build hygiene]].
