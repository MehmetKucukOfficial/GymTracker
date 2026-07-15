---
type: architecture
status: current
implementation_status: current
verified_commit: 21bc385
updated: 2026-07-16
source_files:
  - .github/workflows/android-ci.yml
  - build.gradle.kts
  - app/build.gradle.kts
  - app/proguard-rules.pro
  - gradlew
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.properties
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/gymtracker/di/DatabaseModule.kt
tags: [gymtracker/architecture, gymtracker/build, gymtracker/quality]
---

# Mimari — Build, DI ve Platform

## Toolchain

> Update (2026-07-16): Linux-executable Gradle 8.4 wrapper, root `ciQuality` task, GitHub Actions JVM/lint/debug/release + API 34 device jobs and minified release artifacts are active. Production signing/distribution credentials are intentionally not stored in the repository.

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

- `ciQuality`: 28 JVM testi + lint + debug APK + R8/resource-shrunk release APK/AAB başarılı
- `connectedDebugAndroidTest`: 54/54 başarılı, Pixel 6 Pro API 34; golden karşılaştırmaları dahil
- unsigned release APK: 2,556,733 bayt; debug APK: 19,981,034 bayt
- release AAB: 4,809,602 bayt; R8 `mapping.txt` üretilir

## CI ve release sözleşmesi

- Pull request ve `main` push’larında `actions/checkout@v7`, `actions/setup-java@v5`, `gradle/actions/setup-gradle@v6` kullanılır.
- `quality` job’u tek kaynaklı `ciQuality` görevini çalıştırır ve lint, unsigned minified APK/AAB ile mapping artifact’lerini 14 gün tutar.
- `device-tests` job’u quality sonrasında API 34 Pixel 6 Pro emülatöründe bütün instrumentation/golden takımını çalıştırır ve raporları saklar.
- Release `minifyEnabled` + `shrinkResources` kullanır; geniş keep kuralı yoktur, AndroidX/Room/Hilt/Coil consumer rule’larına dayanır.
- Repository üretim keystore’u veya parolası içermez. Üretilen yerel release APK/AAB imzasızdır; mağaza dağıtımı için ayrı secret-backed signing kapısı gerekir.

## Manifest riskleri

- Kamera/medya izni yok; sistem picker seçili URI için geçici erişim verir ve uygulama internal kopya alır.
- `allowBackup=true`; progress fotoğrafları özel backup/data-extraction kurallarıyla cloud/device transfer dışında tutulur.

## İlk işler

1. İlk hosted GitHub Actions koşusunu yeşil doğrulama
2. Secret-backed production signing ve Play dağıtım politikası
3. Baseline Profile/macrobenchmark ile ölçümlü startup/scroll optimizasyonu
4. Kontrollü dependency güncellemeleri

Yol haritası: [[ROADMAP_EXECUTION_GRAPH#PR 1 — Build hygiene]].
