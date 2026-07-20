---
type: workflow
status: current
implementation_status: active
verified_commit: 26696b9
updated: 2026-07-20
tags: [gymtracker/workflow, gymtracker/impact]
---

# Değişiklik Etki Haritası

## Room entity veya kolon değişikliği

Oku/değiştir: `Entities.kt`, `AppDatabase.kt`, ilgili DAO, repository/domain mapper, feature, schema JSON, Migration.
Zorunlu test: eski tüm sürümler→latest, DAO query, row preservation.
Graf: [[ARCH_DATABASE]], [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]], [[TEST_MATRIX#Room ve migration]].

## DAO query değişikliği

Oku: ilgili entity/index, bütün çağıran ViewModel/repository. `rg "methodName"`.
Test: empty/boundary/order/date zone ve query plan gerekiyorsa.
Graf: [[MOC_DATA]], ilgili feature.

## Navigation route değişikliği

Oku: `MainActivity.kt`, `NavGraph.kt`, `DeepLinkContract.kt`, manifest, `BottomNavigationBar.kt`, `ProgressHubScreen.kt`, route ID alan ilgili ViewModel/repository ve çağıran hızlı erişim/kartlar.
Test: `NavigationContractTest`, `AdaptiveNavigationTest`, `NavigationSmokeTest`, `DeepLinkNavigationTest`, `ProcessDeathNavigationTest`; 599/600 dp, bottom bar/rail, back, detail ana navigasyonu, positive ID, selected tab, Activity recreation, deep link ve ayrı süreçte PID/task restorasyonu.
Graf: [[ARCH_NAVIGATION]], [[ADR_002_FIVE_TARGET_NAVIGATION]].

## Theme veya ortak Compose bileşeni

Oku: `Theme.kt`, `DesignTokens.kt`, `GymTrackerComponents.kt`, platform `values(-night)/themes.xml`, tüm call site’lar, design note.
Test: `HomeLayoutConfigTest`, `GtFeedbackComponentsTest`, ilgili ScreenTest ve golden; light/dark, font 200%, 48 dp touch, TalkBack, screenshot, compact/expanded.
Graf: [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]].

## Ortak top app bar, geri eylemi veya Snackbar

Oku: `GymTrackerComponents.kt`, `MainActivity.kt`, bütün child/detail Screen call site’ları, `NavGraph.kt`, navigation policy ve TR/EN action kaynakları.
Test: `GtFeedbackComponentsTest`, `NavigationSmokeTest`, `CriticalStateGoldenTest` ve ilgili ScreenTest; tek geri semantics’i, 48 dp touch, `%200` fontta kırpılmayan başlık/support, top bar/system back eşdeğerliği, loading/not-found/content, Snackbar mesaj/undo, light/dark ve golden.
Graf: [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]], [[TEST_MATRIX]].

## Feature list-detail veya genişlik eşiği

Oku: `GymTrackerComponents.kt`, ilgili liste/detail Screen, mevcut ID route/ViewModel, TR/EN kaynaklar ve golden fixture’ları.
Test: `GtListDetailLayoutTest`, ilgili ScreenTest ve `AdaptiveListDetailGoldenTest`; 839/840 dp, compact doğrudan route, expanded yerel seçim + açık eylemi, selected semantics, state restoration, light/dark ve büyük font.
Graf: [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], ilgili feature.

## Workout set/session modeli

Oku: Workout feature + DAO/entity + Home + Muscle Load + Programs.
Test: `DatabaseMigrationTest`, `WorkoutRepositoryTest`, `WorkoutScreenTest`, `HomeDashboardRepositoryTest`; active/resume/finish/read-only, program copy, Home update, load calculation.
Graf: [[ADR_006_WORKOUT_SESSION_LIFECYCLE]], [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[FEATURE_MUSCLE_LOAD]], [[FEATURE_PROGRAMS]].

## Exercise adı/kütüphanesi

Oku: `MuscleGroup.kt`, `ProgramExercise.kt`, program seed/ProgramDayExercise, WorkoutSet ve Muscle Load eşlemesi.
Kontrol: duplicate stable ID 0, missing program ref 0, activation validation; gösterim adı değişse de persisted ID değişmez.
Graf: [[FEATURE_WORKOUT]], [[FEATURE_PROGRAMS]], [[FEATURE_MUSCLE_LOAD]].

## Program planı veya program→workout

Oku: `ProgramDao`, `ProgramRepository`, `ProgramSeedInitializer`, `ProgramWorkoutPlan`, `WorkoutDao/Repository`, entity/migration, Programs ve Workout Screen/ViewModel.
Test: v6 JSON→v7 relation + bozuk payload fallback; 5/23/108 seed; locked/ownership/target guard; existing-active duplicate yok; planned set completion/finish; navigation recreation; dark/%200 font.
Graf: [[FEATURE_PROGRAMS]], [[ADR_007_RELATIONAL_PROGRAM_PLANS]], [[ARCH_DATABASE]], [[FEATURE_WORKOUT]], [[FEATURE_MUSCLE_LOAD]].

## Fotoğraf saklama

Oku: Progress Screen/VM/repository/DAO/entity, `PhotoFileStorage`, manifest, backup/data extraction rules, Coil config ve Room photo media metadata/migration.
Test: provider, EXIF orientation, bounded decode/resize, thumbnail dimension, iki dosyalı import/DB rollback, 25 MB kaynak + 48 MB reserve sınırı, orphan/missing-main/missing-thumbnail/metadata-only restore reconciliation, backup XML, legacy v0/v1, gerçek host reboot + boot counter, bozuk dosya, delete cleanup, 500-row lazy grid, FrameTiming/Perfetto, dark/%200 font.
Graf: [[FEATURE_PROGRESS_PHOTOS]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]].

## Kas yükü hesabı veya haritası

Oku: `WorkoutDao`, `CompletedSetForLoad`, `MeasurementDao`, `MuscleLoadCalculator`, Fatigue ViewModel/Screen, `MuscleBodyMap`, exercise library ve Workout lifecycle sözleşmesi.
Test: completed/active/incomplete ayrımı, recency, latest valid personal bodyweight + fallback, invalid ölçüm, unknown exercise, cap/level, iki Flow’un reaktivitesi, front/back map, renk bağımsız liste, dark/%200 font.
Graf: [[FEATURE_MUSCLE_LOAD]], [[ADR_005_MUSCLE_LOAD_NAMING]], [[FEATURE_WORKOUT]], [[ARCH_STATE_DATA_FLOW]].

## Tarih veya zaman hesabı

Oku: `TimeModule.kt`, `HomeDashboardPeriod.kt`, `NutritionRules.kt`, ilgili ViewModel, Fatigue/Load ve Measurement date queries.
Test: Istanbul/Berlin DST geçmiş örnekleri, 23/25 saat, injectable Clock ile midnight, runtime zone change, inclusive/exclusive boundary, SavedStateHandle/process recreation ve değer taşıyan gerçek OS process death.
Graf: [[ARCH_STATE_DATA_FLOW]], feature notları.

## Nutrition veya Measurements input/state/edit/undo/chart

Oku: ilgili Screen/ViewModel/DAO, `MeasurementRepository`, `LocalizedDecimal.kt`, Nutrition/Measurement rules, Home aggregate çağıranları ve TR/EN resources.
Test: rules + `MutationContractTest`, `HomeDashboardRepositoryTest`, Screen/locale testleri; malformed restore, snapshot scale, aynı-ID undo, empty, comma/dot, min/max, zero/over-goal, period boundary, latest/history, 0/1/2+ chart, light/dark/%200 font, second locale ve seçili tarih/period gerçek process death.
Graf: [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]].

## Gradle/dependency/SDK

Oku: root/app build, version catalog, wrapper props/scripts, ProGuard, manifest ve `.github/workflows/android-ci.yml`.
Test: `ciQuality`, API 34 device/golden, R8 mapping, APK/AAB output ve release manifest; signing değişikliği varsa secret erişimi olmadan fail-fast ve ayrı dağıtım smoke’u. Major güncellemeyi ayrı PR yap.
Graf: [[ARCH_BUILD_DI]], [[ROADMAP_EXECUTION_GRAPH]].

## Release signing veya distribution

Oku: `app/build.gradle.kts`, `.github/workflows/android-release.yml`, `.gitignore`, production environment secret adları ve artifact yolları.
Test: secretsiz fail-fast; geçici test keystore ile `releaseDistribution`; APK `apksigner`, AAB `jarsigner`, embedded profile ve mapping; gerçek key materyalini/log secret’larını repoya alma.
Graf: [[ADR_009_SECRET_BACKED_RELEASES]], [[ARCH_BUILD_DI]], [[RISK_REGISTER_GRAPH]].

## Baseline Profile veya macrobenchmark

Oku: `baselineprofile/`, app baseline plugin/dependency, generated profile kaynakları ve kritik UI semantics/navigation metinleri.
Test: `generateBaselineProfile`; startup None/Partial, Home scroll, Workout navigation ve fixture grain’ini açıkça doğrulayan büyük-grid FrameTiming; release APK/AAB embedded profile. `ProcessDeathNavigationTest` değişirse temiz DB üzerinde PID değişimi + aynı task restorasyonunu çalıştır. Photo benchmark değişirse önce 120-row seed/reboot/verify fazını çalıştır; benchmark JSON + Perfetto trace artifact’lerini sakla. Emülatör performans sonucu yalnız ön baseline, performans kabulü fiziksel release cihazında.
Graf: [[PERFORMANCE_BASELINE]], [[TEST_MATRIX]], ilgili feature.

## String/locale/birim

Oku: tüm hardcoded kullanıcı metinleri, formatter, validation, setting DataStore.
Test: `ResourceCoverageTest`, `LocalizationResourcesTest`, locale bağımsız UI testleri ve ilgili golden; Türkçe + English anahtar eşitliği, plural, decimal, para ve kg/lb.
Graf: [[DESIGN_SYSTEM_GRAPH]], ilgili feature.

## Settings veya Preferences DataStore

Oku: `data/settings/AppSettings.kt`, `di/SettingsModule.kt`, Settings Screen/ViewModel, `MainActivity.kt`, `Theme.kt`, etkilenen gerçek feature tüketicileri ve TR/EN kaynakları.
Test: settings fallback/round-trip, gerçek davranış tüketimi, Activity recreation, navigation back, light/dark, `%200` font ve `ResourceCoverageTest`. Henüz davranışa bağlanmayan tercih UI’a eklenmez.
Graf: [[FEATURE_SETTINGS_DATA]], [[ARCH_STATE_DATA_FLOW]], [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]].
