---
type: workflow
status: current
implementation_status: active
verified_commit: 4d0cad8
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
Test: active/resume/finish, program copy, Home update, load calculation.
Graf: [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[FEATURE_MUSCLE_LOAD]], [[FEATURE_PROGRAMS]].

## Exercise adı/kütüphanesi

Oku: `MuscleGroup.kt`, program seed referansları, fatigue calculator.
Kontrol: duplicate isim 0, missing program ref 0, activation validation.
Hedef: string yerine kararlı ID.
Graf: [[FEATURE_WORKOUT]], [[FEATURE_PROGRAMS]], [[FEATURE_MUSCLE_LOAD]].

## Fotoğraf saklama

Oku: Progress Screen/VM/DAO/entity, manifest, backup rules, Coil config.
Test: provider, restart, reboot, bozuk dosya, low storage, delete cleanup.
Graf: [[FEATURE_PROGRESS_PHOTOS]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]].

## Tarih veya zaman hesabı

Oku: Home, Nutrition, Fatigue/Load, Measurement date queries.
Test: Istanbul DST geçmiş örnekleri, midnight, zone change, inclusive/exclusive boundary.
Graf: [[ARCH_STATE_DATA_FLOW]], feature notları.

## Gradle/dependency/SDK

Oku: root/app build, version catalog, wrapper props, manifest.
Test: clean compile, unit, lint, debug/release; major güncellemeyi ayrı PR.
Graf: [[ARCH_BUILD_DI]], [[ROADMAP_EXECUTION_GRAPH]].

## String/locale/birim

Oku: tüm hardcoded kullanıcı metinleri, formatter, validation, setting DataStore.
Test: Turkish + second locale, plural, decimal, kg/lb.
Graf: [[DESIGN_SYSTEM_GRAPH]], ilgili feature.
