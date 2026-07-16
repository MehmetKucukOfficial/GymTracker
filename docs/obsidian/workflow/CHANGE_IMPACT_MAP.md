---
type: workflow
status: current
implementation_status: active
verified_commit: 7917358
updated: 2026-07-15
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

Oku: `MainActivity.kt`, `NavGraph.kt`, `BottomNavigationBar.kt`, `ProgressHubScreen.kt`, route ID alan ilgili ViewModel/repository ve çağıran hızlı erişim/kartlar.
Test: `NavigationContractTest`, `NavigationSmokeTest`; back, detail alt barı, positive ID, selected tab, Activity/process recreation ve deep link.
Graf: [[ARCH_NAVIGATION]], [[ADR_002_FIVE_TARGET_NAVIGATION]].

## Theme veya ortak Compose bileşeni

Oku: `Theme.kt`, `DesignTokens.kt`, `GymTrackerComponents.kt`, platform `values(-night)/themes.xml`, tüm call site’lar, design note.
Test: `HomeLayoutConfigTest`, `HomeScreenTest`; light/dark, font 200%, TalkBack, screenshot, compact/expanded.
Graf: [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]].

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

Oku: Progress Screen/VM/repository/DAO/entity, `PhotoFileStorage`, manifest, backup/data extraction rules, Coil config.
Test: provider, import/DB rollback, limit, orphan/missing reconciliation, legacy v0, restart, reboot, bozuk dosya, low storage, delete cleanup, dark/%200 font.
Graf: [[FEATURE_PROGRESS_PHOTOS]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]].

## Kas yükü hesabı veya haritası

Oku: `WorkoutDao`, `CompletedSetForLoad`, `MuscleLoadCalculator`, Fatigue ViewModel/Screen, `MuscleBodyMap`, exercise library ve Workout lifecycle sözleşmesi.
Test: completed/active/incomplete ayrımı, recency, bodyweight fallback, unknown exercise, cap/level, Flow reactivity, front/back map, renk bağımsız liste, dark/%200 font.
Graf: [[FEATURE_MUSCLE_LOAD]], [[ADR_005_MUSCLE_LOAD_NAMING]], [[FEATURE_WORKOUT]], [[ARCH_STATE_DATA_FLOW]].

## Tarih veya zaman hesabı

Oku: `TimeModule.kt`, `HomeDashboardPeriod.kt`, `NutritionRules.kt`, ilgili ViewModel, Fatigue/Load ve Measurement date queries.
Test: Istanbul/Berlin DST geçmiş örnekleri, 23/25 saat, midnight, zone change, inclusive/exclusive boundary, SavedStateHandle/process recreation.
Graf: [[ARCH_STATE_DATA_FLOW]], feature notları.

## Nutrition veya Measurements input/state

Oku: ilgili Screen/ViewModel/DAO, `LocalizedDecimal.kt`, `NutritionRules.kt` veya `MeasurementRules.kt`, Home aggregate çağıranları ve string resources.
Test: `LocalizedDecimalTest`, `NutritionRulesTest`, `MeasurementRulesTest`, `NutritionScreenTest`, `MeasurementsScreenTest`; empty, comma/dot, min/max, zero/over-goal, latest/history, 0/1/2+ trend, light/dark ve %200 font.
Graf: [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]].

## Gradle/dependency/SDK

Oku: root/app build, version catalog, wrapper props, manifest.
Test: clean compile, unit, lint, debug/release; major güncellemeyi ayrı PR.
Graf: [[ARCH_BUILD_DI]], [[ROADMAP_EXECUTION_GRAPH]].

## String/locale/birim

Oku: tüm hardcoded kullanıcı metinleri, formatter, validation, setting DataStore.
Test: Turkish + second locale, plural, decimal, kg/lb.
Graf: [[DESIGN_SYSTEM_GRAPH]], ilgili feature.
