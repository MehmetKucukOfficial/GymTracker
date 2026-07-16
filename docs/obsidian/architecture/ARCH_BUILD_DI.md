---
type: architecture
status: current
implementation_status: current
verified_commit: 0fa2383
updated: 2026-07-16
source_files:
  - .github/workflows/android-ci.yml
  - .github/workflows/android-release.yml
  - build.gradle.kts
  - app/build.gradle.kts
  - app/proguard-rules.pro
  - gradlew
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.properties
  - app/src/main/AndroidManifest.xml
  - baselineprofile/build.gradle.kts
  - app/src/release/generated/baselineProfiles/baseline-prof.txt
  - app/src/main/java/com/gymtracker/di/DatabaseModule.kt
tags: [gymtracker/architecture, gymtracker/build, gymtracker/quality]
---

# Mimari — Build, DI ve Platform

## Toolchain

> Update (2026-07-16): Linux-executable Gradle 8.4 wrapper, root `ciQuality`, hosted quality/device jobs, protected production release workflow and measured Baseline Profile producer are active. Production credentials are intentionally not stored in the repository.

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

- `ciQuality`: 30 JVM testi + lint + debug APK + R8/resource-shrunk release APK/AAB başarılı
- `connectedDebugAndroidTest`: 58/58 başarılı, Pixel 6 Pro API 34; dört golden karşılaştırması dahil
- unsigned release APK: 2,556,733 bayt; debug APK: 19,981,034 bayt
- release AAB: 4,809,602 bayt; R8 `mapping.txt` üretilir
- `verifyReleaseProfiles`: APK `assets/dexopt/baseline.prof` ve AAB `BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof` girdilerini doğrular
- Profil üretici: startup + Home scroll + Workout CUJ; macrobenchmark cold startup None/Partial ve iki frame yolculuğunu ölçer

## CI ve release sözleşmesi

- Pull request ve varsayılan `master` push’larında `actions/checkout@v7`, `actions/setup-java@v5`, `gradle/actions/setup-gradle@v6` kullanılır.
- `quality` job’u tek kaynaklı `ciQuality` görevini çalıştırır ve lint, unsigned minified APK/AAB ile mapping artifact’lerini 14 gün tutar.
- `device-tests` job’u quality sonrasında API 34 Pixel 6 Pro emülatöründe bütün instrumentation/golden takımını çalıştırır ve raporları saklar.
- Release `minifyEnabled` + `shrinkResources` kullanır; geniş keep kuralı yoktur, AndroidX/Room/Hilt/Coil consumer rule’larına dayanır.
- Repository üretim keystore’u veya parolası içermez. Üretilen yerel release APK/AAB imzasızdır; mağaza dağıtımı için ayrı secret-backed signing kapısı gerekir.
- Manuel `android-release.yml`, korumalı `production` environment’ında base64 keystore’u runner temp alanında oluşturur; dört secret eksikse fail-fast olur, `releaseDistribution` APK v2/v3 + AAB JAR imzasını doğrular ve keystore’u her durumda siler.
- Normal PR CI unsigned kalır. Gerçek signing yalnız manuel production workflow’unda etkinleşir; karar [[ADR_009_SECRET_BACKED_RELEASES]].
- Generated Baseline/Startup Profile kaynakta tutulur; release build R8 sonrası profile’ı paketler. Ölçüm sözleşmesi [[PERFORMANCE_BASELINE]].

## Manifest riskleri

- Kamera/medya izni yok; sistem picker seçili URI için geçici erişim verir ve uygulama internal kopya alır.
- `allowBackup=true`; progress fotoğrafları özel backup/data-extraction kurallarıyla cloud/device transfer dışında tutulur.

## İlk işler

1. Gerçek production secret’larını environment’a tanımlayıp Play internal-track smoke’u
2. Fiziksel referans cihazda startup/scroll/Workout benchmark kabulü
3. Branch protection içinde quality + API 34 job’larını required check yapma
4. Kontrollü dependency güncellemeleri

Hosted kabul: PR #10 koşusu `29474876318` üzerinde quality işi 11 dk 08 sn, API 34 cihaz/golden işi 14 dk 06 sn sürdü; 30 JVM ve 58 instrumentation testi sıfır hatayla geçti. Linux runner KVM erişimi workflow içinde açıkça hazırlanır. PR #1–#10 merge commit ile `master`a alındı; push trigger’ındaki eski `main` adı `master` olarak düzeltildi.

Yol haritası: [[ROADMAP_EXECUTION_GRAPH#PR 1 — Build hygiene]].
