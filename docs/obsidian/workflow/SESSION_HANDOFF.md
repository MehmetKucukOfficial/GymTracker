---
type: handoff
status: ready
implementation_status: active
code_baseline: 3f2f8cf
updated: 2026-07-16
active_phase: "Phase 6 - PR1-PR11 merged; master accepted"
next_pr: "Wait for user direction"
primary_nodes:
  - ARCH_NAVIGATION
  - DESIGN_SYSTEM_GRAPH
  - FEATURE_PROGRAMS
  - FEATURE_WORKOUT
  - TEST_MATRIX
  - RISK_REGISTER_GRAPH
  - ROADMAP_EXECUTION_GRAPH
tags: [gymtracker/workflow, gymtracker/session]
---

# Oturum Handoff

## Şu anki durum

- GitHub PR #1–#11, 2026-07-16 tarihinde merge commit stratejisiyle sırayla `master`a alındı; son code baseline `3f2f8cf`, açık PR kalmadı ve first-parent geçmişinde on bir merge commit korunuyor.
- PR #11, workflow push filtresini `main` yerine varsayılan `master` dalıyla eşledi. Otomatik `master` koşusu `29499237460` tamamen yeşil; artifact XML 58/58 cihaz testi gösteriyor.
- Uygulama baseline: `f9d6d75`; PR8 `codex/workout-lifecycle` dalında draft PR #1 olarak yayınlandı.
- PR9 baseline: `341bf79`; `codex/nutrition-measurements` dalı PR8’i hedefleyen stacked draft PR #2 olarak yayınlandı.
- PR10 baseline: `2ad7ad8`; `codex/progress-load` dalı PR9’u hedefleyen stacked draft PR #3 olarak yayınlandı.
- PR11 baseline: `7917358`; `codex/programs-normalization` dalı PR10’u hedefleyen stacked draft PR #4 olarak yayınlandı.
- PR12 baseline: `9c90471`; `codex/nutrition-measurements-depth` dalı PR11’i hedefleyen stacked draft PR #5 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/5
- PR13 code baseline: `5c3ce35`; graph baseline `d189f6c`; `codex/photo-load-calibration` dalı PR12’yi hedefleyen stacked draft PR #6 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/6
- PR14 code baseline: `41c2ceb`; graph baseline `f65e162`; `codex/adaptive-locale-goldens` dalı PR13’ü hedefleyen stacked draft PR #7 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/7
- PR15 code baseline: `abd3fbe`; graph baseline: `cd331c8`; `codex/ci-release-hardening` dalı PR14’ü hedefleyen stacked draft PR #8 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/8
- PR16 code baseline: `a2586d1`; `codex/release-performance` dalı PR15’i hedefleyen stacked draft PR #9 olarak yayınlandı ve hosted run `29473550009` iki işiyle yeşil: https://github.com/MehmetKucukOfficial/GymTracker/pull/9
- PR17 code baseline: `a573ab9`; `codex/list-detail-goldens` dalı PR16’yı hedefleyen stacked draft PR #10 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/10
- Room v9; explicit 8→9 photo thumbnail/dimension metadata migration aktif, destructive fallback yok; v0/v1 fotoğraf satırları kayıpsız korunur.
- Compact navigation shell’i 5 ana hedefli bottom bar, 600 dp+ shell aynı hedeflerle rail kullanıyor; Workout/Programs 840 dp+ ortak liste–özet düzenine geçiyor; full detail hâlâ ID + SavedStateHandle + repository Flow kullanıyor.
- Home completed/active workout, measurement ve nutrition kaynaklarına reaktif; aktif workout varsa CTA doğrudan session ID’ye devam ediyor.
- Workout tek aktif seans, status-korumalı mutation, COMPLETED salt-okunur geçmiş ve adaptive modern UI kullanıyor.
- Nutrition DST-safe tarih aralığı, SavedStateHandle tarih seçimi, snapshot-safe amount/meal edit, delete undo, locale sayı/tarih ve TR/EN adaptive UI kullanıyor.
- Measurements repository + indexed 7/30/90/tümü query, gerçek kilo chart, özel tarih, edit/delete undo, latest/history/trend ve TR/EN adaptive UI kullanıyor.
- Progress fotoğrafları cihaz-yerel backup politikası, bounded decode, EXIF normalize, 2048 ana/512 thumbnail, iki dosyalı rollback/reconciliation, viewer ve adaptive grid kullanıyor.
- Muscle Load completed-set Flow’unu latest-valid measurement Flow’uyla birleştirir; recency/personal-bodyweight/fallback/unknown sözleşmesi, kalibrasyon kaynağı, metodoloji ve erişilebilir liste aktiftir.
- Programs locked/ownership/target kurallarını repository’de doğrular; program günü atomik ACTIVE workout’a planlanan set olarak kopyalanır, tamamlanmayan set Muscle Load’a girmez ve finish’i kilitler.
- Light/dark tasarım sistemi token temeli ve ortak Home bileşenleri aktif; Home loading/content/error, retry, midnight period tick ve adaptive kolon kullanıyor.
- Uygulama geneli Türkçe/English kaynak kataloğu 356/356 eşit; muscle adları ve program para biçimi locale-aware.
- Test temeli aktif: 30 JVM + 58 API 34 instrumentation testi geçiyor; Progress Hub, Programs ve Workout için dört kalıcı golden baseline var.
- Debug build başarılı; lint 0 error; assemble başarılı.
- `ciQuality` JVM/lint/debug ile R8/resource-shrunk unsigned APK, AAB ve mapping üretir; GitHub Actions quality + API 34 device/golden workflow’u kaynakta.

## Sıradaki önerilen iş

Birleştirme ve nihai `master` kabulü tamamlandı. Yeni geliştirme başlatmadan kullanıcı komutunu bekle.

## Açık P0/P1

- Legacy `storageVersion=0` fotoğraflar için yeniden seçim ve v1 re-optimize UX’i
- GitHub branch protection içinde quality + API 34 check’lerini zorunlu yapma
- Golden baseline’ı kalan kritik ekran/state’lere genişletme
- Harici deep link ve gerçek OS process-death
- Gerçek production secret/Play internal-track smoke ve fiziksel cihaz benchmark kabulü

## Son doğrulama

- `testDebugUnitTest`: success, 30/30.
- `connectedDebugAndroidTest`: success, 58/58, Pixel 6 Pro API 34 (6m21s).
- `lintDebug`: success, 0 error.
- `assembleDebug`: success.
- `ciQuality`: success; minified APK 2,556,733 bayt, AAB 4,809,602 bayt ve R8 mapping üretildi.
- Kalıcı görsel kapı: iki Progress Hub baseline’ına ek olarak `programs_list_detail_light_en.png` ve `workout_list_detail_dark_tr_font130.png`, API 34 üzerinde pixel tolerance ile geçti.

## Handoff güncelleme şablonu

```text
Completed:
Files changed:
Tests run/results:
Risks changed:
Graph notes updated:
Next concrete action:
Blockers:
```

## 2026-07-16 - PR #1–#11 merge consolidation and master acceptance

Completed:
- Marked each stacked draft ready, merged it with GitHub merge-commit strategy, then retargeted the child PR to `master` before continuing.
- Verified every retargeted child retained only its own commits and returned `CLEAN/MERGEABLE`; no source branch was deleted during the chain.
- Preserved merge ancestry in `master`: #1 `e2741b2`, #2 `fb01c19`, #3 `6e9a310`, #4 `b8a6966`, #5 `ad112a2`, #6 `565b515`, #7 `483d36a`, #8 `601206b`, #9 `a92a83c`, #10 `be2d8c5`.
- Found and fixed the hosted push-filter mismatch from non-existent `main` to default `master` in code commit `0fa2383`.
- Published PR #11, verified its hosted checks, and merged it with merge commit `3f2f8cf`; no open PR remains and source branches were retained.

Tests run/results:
- Pre-merge cumulative PR #10 run `29474876318`: quality 11m08s; API 34 device/golden 14m06s; artifact XML 58/58.
- Manual combined-master run `29497428633` was intentionally cancelled to avoid duplicate compute after the PR validation path was selected.
- PR #11 run `29497592820`: quality 10m49s; API 34 device/golden 13m23s; artifact XML 58/58.
- Automatic `master` push run `29499237460` against `3f2f8cf`: quality 11m54s; API 34 device/golden 9m26s; artifact XML 58 tests, 0 failure/error/skipped.

Risks changed:
- Deep stacked-PR integration risk is closed; all eleven PRs are now represented by merge commits on `master`.
- R3 improves to resolved-core+ after matching the automatic push trigger to the actual default branch; branch-protection required checks remain repository administration work.

Next concrete action:
- Wait for the user's next development direction; do not start another PR autonomously.

Blockers:
- No code blocker.

## 2026-07-16 - PR 17 adaptive list-detail + golden expansion

Completed:
- Added shared 840 dp `GtListDetailScaffold` with deterministic 839/840 policy and 54/46 pane split.
- Preserved compact direct ID-route behavior; expanded Workout/Programs card selection updates an in-place preview and the explicit action opens the existing stable-ID detail route.
- Added `rememberSaveable` program/session selection, selected card semantics and color-independent border emphasis, scrollable preview panes and localized actions.
- Added English/light Programs and Turkish/dark/%130 Workout 1000×700 permanent golden baselines.

Files changed:
- `GymTrackerComponents.kt`, Programs/Workout screens, TR/EN resources
- `GtListDetailLayoutTest`, Programs/Workout Compose tests, `AdaptiveListDetailGoldenTest` and two PNG assets
- Related feature, navigation, design, quality, risk, roadmap, atlas, impact, start and handoff nodes

Tests run/results:
- `testDebugUnitTest`: 30/30, 0 failures.
- Targeted Programs/Workout Compose suite: 9/9, 0 failures.
- Targeted adaptive golden suite: 2/2, 0 failures after committed baseline generation and visual inspection.
- Full `connectedDebugAndroidTest`: 58/58, 0 failures on Pixel 6 Pro API 34 in 6m21s.
- `ciQuality`: success including lint, debug, minified release APK/AAB/mapping and embedded profile verification.
- Resource contract: default 356, English 356, key diff 0.
- Hosted GitHub Actions run `29474876318`: quality 11m08s; API 34 device/golden 14m06s; artifact XML 58 tests, 0 failures/errors/skipped.

Risks changed:
- R13 resolved+: compact bottom bar, 600 dp+ rail and 840 dp+ Workout/Programs list-detail preserve one route/save-restore contract.
- R15 mitigated++: four permanent golden baselines, selected semantics and expanded feature layout are active; remaining critical state coverage stays open.
- No Room schema, DAO, repository or route definition changed.

Next concrete action:
- Draft PR #10 yayınlandı ve hosted run yeşil; kullanıcı komutunu bekle.

Blockers:
- No code blocker. Production-key/Play access, physical benchmark hardware, external deep links and true OS process death remain external/follow-up acceptance inputs.

## 2026-07-16 - PR 16 secret-backed release + performance baseline

Completed:
- Added protected manual production workflow, atomic four-secret signing configuration, runner-temp keystore cleanup and signed APK/AAB artifact retention.
- Added Gradle fail-fast, APK v2/v3, AAB JAR signature and embedded Baseline Profile verification gates.
- Added `:baselineprofile` producer with shared startup/Home scroll/Workout CUJs, generated release Baseline/Startup Profiles and four macrobenchmark scenarios.

Files changed:
- `.github/workflows/android-release.yml`, root/app/version-catalog/settings Gradle files, `.gitignore`
- `baselineprofile/`, `app/src/release/generated/baselineProfiles/`
- Related build, ADR, performance, test, risk, roadmap, atlas, impact, MOC, start and handoff nodes

Tests run/results:
- `ciQuality`: success, including embedded APK/AAB profile verification.
- `connectedDebugAndroidTest`: 54/54 success after clean AVD reboot; prior run reached 50/54 then emulator OS aborted with `DeadSystemException`, no product assertion failure.
- Baseline generation: 2 producer tests, 0 failures; 4 macrobenchmark scenarios execute on API 34 emulator.
- Dummy keystore `releaseDistribution`: APK v2/v3 + AAB signature + embedded profile success; temporary key deleted.
- Hosted GitHub Actions run `29473550009`: quality job 11m05s and API 34 device/golden job 12m47s, both successful.

Risks changed:
- R16 mitigated-config; real protected secrets and Play internal-track smoke remain operational.
- R17 partial; emulator performance baseline exists, physical release-device acceptance remains open.

Next concrete action:
- Implement PR17 list-detail and golden expansion on a branch stacked on `codex/release-performance`; keep monitoring PR16 hosted CI.

Blockers:
- No code blocker. Real production key/Play access and physical benchmark hardware are external operational inputs.

## 2026-07-14 - PR 1A build reproducibility

Completed:
- Restored the Gradle 8.4 wrapper scripts and wrapper JAR.
- Added the project README with the JDK 17 requirement and build commands.
- Added the `app/proguard-rules.pro` file referenced by the release build type.

Files changed:
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`
- `README.md`
- `app/proguard-rules.pro`

Tests run/results:
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-17 .\\gradlew.bat --version`: Gradle 8.4 / JVM 17.0.10.
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-17 .\\gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Risks changed:
- R3 build reproducibility is partially reduced; lint and CI remain open.

Graph notes updated:
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 1B: inspect actual photo acquisition usage, remove unsupported or unused media permissions, then run `lintDebug` and fix the remaining lint error without adding a baseline.

Blockers:
- None for PR 1A. System `java` still resolves to JDK 11, so use JDK 17 explicitly until the developer environment is updated.

## 2026-07-14 - PR 1B manifest and lint gate

Completed:
- Removed unused `CAMERA` and `READ_MEDIA_IMAGES` permissions from the manifest.
- Preserved the system-picker flow (`ActivityResultContracts.GetContent`), which receives URI access from the selected provider and does not need either permission.
- Removed five unused `*_temp.png` body-mask resources.

Files changed:
- `app/src/main/AndroidManifest.xml`
- Five unused `app/src/main/res/drawable-nodpi/*_temp.png` files

Tests run/results:
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-17 .\\gradlew.bat lintDebug`: BUILD SUCCESSFUL, 0 errors / 40 warnings.
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-17 .\\gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Risks changed:
- R3 lint error is resolved without a lint baseline.
- R14 unnecessary sensitive media/camera permissions are removed.
- R2 remains open: the current `GetContent()` URI is not persisted across restart.

Graph notes updated:
- [[ARCH_BUILD_DI]]
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 2: establish the unit-test foundation, then add the first deterministic tests for a pure utility/validator and a Room DAO.

Blockers:
- No code blocker. CI is intentionally deferred until the test foundation task.

## 2026-07-14 - PR 2 initial test foundation

Completed:
- Added JUnit JVM-test and AndroidJUnitRunner/Room instrumentation-test dependencies.
- Added two deterministic JVM tests for [[FatigueCalculatorTest]].
- Added two Room DAO instrumentation tests in [[WorkoutDaoTest]].

Files changed:
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/test/java/com/gymtracker/domain/FatigueCalculatorTest.kt`
- `app/src/androidTest/java/com/gymtracker/data/dao/WorkoutDaoTest.kt`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 2 tests, 0 failures.
- `assembleDebugAndroidTest`: BUILD SUCCESSFUL; test APK generated.
- The instrumentation tests were compiled but not run because no Android device/emulator was attached.
- `lintDebug` and `assembleDebug`: BUILD SUCCESSFUL; lint has 0 errors / 49 warnings.

Risks changed:
- R3 no longer has an empty JVM test task; CI integration remains open.
- The Room query/cascade test pattern is available for migration and seed-safety work.

Graph notes updated:
- [[TEST_MATRIX]]
- [[ARCH_DATABASE]]
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 3: remove destructive Room migration fallback, enable schema export, and add explicit migration tests before changing database entities.

Blockers:
- A connected Android device/emulator or CI runner is required to execute `connectedDebugAndroidTest`.

## 2026-07-14 - PR 3 safe Room baseline

Completed:
- Enabled Room schema export and generated the version-controlled v3 schema.
- Removed `fallbackToDestructiveMigration()` from production database creation.
- Added the central `DatabaseMigrations.ALL` registry for future migration edges.
- Added a v3 baseline test that inserts a fixture, reopens with Room, and verifies preserved fields.
- Confirmed the generated schema hash matches the pre-existing emulator database.

Files changed:
- `app/build.gradle.kts`
- `app/src/main/java/com/gymtracker/data/database/AppDatabase.kt`
- `app/src/main/java/com/gymtracker/data/database/DatabaseMigrations.kt`
- `app/src/main/java/com/gymtracker/di/DatabaseModule.kt`
- `app/src/androidTest/java/com/gymtracker/data/database/DatabaseMigrationTest.kt`
- `app/schemas/com.gymtracker.data.database.AppDatabase/3.json`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 2 tests, 0 failures.
- `assembleDebugAndroidTest`: BUILD SUCCESSFUL.
- `connectedDebugAndroidTest` on Pixel 6 Pro API 34: 3 tests, 0 failures.
- Instrumented coverage: v3 baseline row preservation, recent-session query boundary, session-to-set cascade.
- `assembleDebug`: BUILD SUCCESSFUL.

Risks changed:
- R1 destructive deletion is removed for the evidence-backed v3+ support window.
- Unknown v1/v2 databases fail closed rather than being erased.
- v1/v2 migration compatibility cannot be claimed because no historical schema, APK, Git commit, or database fixture exists.

Graph notes updated:
- [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]]
- [[ARCH_DATABASE]]
- [[TEST_MATRIX]]
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 4: move program seeding out of `ProgramsViewModel` into an idempotent, transactional data-layer initializer with repeated/concurrent tests.

Blockers:
- Historical v1/v2 schema artifacts are unavailable. Recover an old APK/database before adding those migration edges; do not infer their SQL.
- `connectedDebugAndroidTest` resets the debug app package on the emulator. The current debug build was reinstalled and opened after the test run.

## 2026-07-14 - PR 4 transactional program seed

Completed:
- Moved default program content and seed orchestration out of `ProgramsViewModel`.
- Added application-start `ProgramSeedInitializer` with a single Room transaction.
- Added v4 `seed_metadata` with an atomic insert-or-ignore claim.
- Added `3 -> 4` migration and generated the v4 Room schema.
- Existing databases with any program retain their data and receive a completion marker instead of duplicate defaults.

Files changed:
- `GymTrackerApp.kt`, `ProgramsViewModel.kt`
- `data/programs/ProgramSeedInitializer.kt`
- `ProgramDao.kt`, `Entities.kt`, `AppDatabase.kt`, `DatabaseMigrations.kt`
- `DatabaseMigrationTest.kt`, `ProgramSeedInitializerTest.kt`
- `app/schemas/.../4.json`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 2 tests, 0 failures.
- `connectedDebugAndroidTest` on Pixel 6 Pro API 34: 5 tests, 0 failures.
- Instrumented coverage: v3 -> v4 row preservation, metadata table creation, repeated/concurrent seed, legacy-program preservation, DAO query/cascade.
- Fresh emulator launch after reinstall: DB v4, 5 programs, 23 program workouts.
- `lintDebug` and `assembleDebug`: BUILD SUCCESSFUL; lint has 0 errors / 49 warnings.

Risks changed:
- R4 seed duplicate and partial-write risk is resolved for the current default content.
- Current content version is immutable (`default-programs-v1`); a later content update must use an explicit new version/key and tests.

Graph notes updated:
- [[FEATURE_PROGRAMS]]
- [[ARCH_DATABASE]]
- [[TEST_MATRIX]]
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 5: make progress photos durable by copying picker content into app-owned storage, storing metadata, and handling delete/restart/error states.

Blockers:
- `connectedDebugAndroidTest` resets the debug app package on the emulator. Reinstall the debug APK after every connected test run.

## 2026-07-14 - PR 5 durable progress photo storage

Completed:
- Added a repository boundary between the progress UI and Room/file operations.
- Picker content is copied on `Dispatchers.IO` into `filesDir/progress_photos` through a temporary file before the Room row is inserted.
- Added a 25 MB import limit, image MIME validation, startup cleanup for interrupted temporary files and managed-path-only deletion.
- Added v5 photo metadata (`mimeType`, `sizeBytes`, `storageVersion`) and the explicit 4→5 migration.
- Legacy rows remain intact as `storageVersion=0`; new app-owned imports use `storageVersion=1`.
- Added saving/error/success feedback to the progress screen.

Files changed:
- `data/photos/PhotoSourceReader.kt`, `PhotoFileStorage.kt`, `ProgressPhotoRepository.kt`
- `di/PhotoDataModule.kt`
- `ProgressViewModel.kt`, `ProgressScreen.kt`
- `ProgressPhotoDao.kt`, `Entities.kt`, `AppDatabase.kt`, `DatabaseMigrations.kt`
- `DatabaseMigrationTest.kt`, `ProgressPhotoRepositoryTest.kt`
- `app/schemas/.../5.json`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 2 tests, 0 failures.
- `connectedDebugAndroidTest` on Pixel 6 Pro API 34: 9 tests, 0 failures.
- Photo coverage: durable copy/metadata, storage recreation, delete cleanup, provider failure, DB insert rollback and oversize temporary-file cleanup.
- `lintDebug` and `assembleDebug`: BUILD SUCCESSFUL; lint has 0 errors / 49 warnings.

Risks changed:
- R2 is mitigated for all new imports; the app no longer relies on a provider URI after save.
- Legacy `storageVersion=0` rows, Android backup policy, orphan reconciliation and the EXIF/thumbnail pipeline remain explicit follow-up work.

Graph notes updated:
- [[ADR_004_PERSISTENT_PHOTO_STORAGE]]
- [[FEATURE_PROGRESS_PHOTOS]]
- [[ARCH_DATABASE]]
- [[TEST_MATRIX]]
- [[RISK_REGISTER_GRAPH]]
- [[ROADMAP_EXECUTION_GRAPH]]
- [[SESSION_HANDOFF]]

Next concrete action:
- PR 6: implement the navigation and lifecycle-aware state skeleton, beginning with route inventory/tests and the five-target shell.

Blockers:
- None for PR 5. `connectedDebugAndroidTest` resets the debug package; reinstall/open the APK after each run.

## 2026-07-15 - PR 6 navigation and reactive-state skeleton

Completed:
- Replaced the seven-item phone bottom bar with Özet, Antrenman, Programlar, Beslenme and İlerleme.
- Added an İlerleme hub; measurements, durable photos and muscle load remain nested routes with the correct selected parent tab.
- Added central `NavigationPolicy` for nested ownership and detail bottom-bar visibility.
- Converted workout and program detail to positive-ID routes backed by `SavedStateHandle` and repository Room Flow state.
- Added `WorkoutRepository`, `ProgramRepository` and `HomeDashboardRepository` boundaries.
- Replaced Home’s measurement/nutrition snapshots with a three-source reactive `combine` flow.
- Added calendar/ZoneId-based day boundaries and removed the 86,400,000 ms day assumption.
- Converted all primary screen Flow collection to `collectAsStateWithLifecycle()`.

Files changed:
- `MainActivity.kt`, `ui/navigation/NavGraph.kt`, `ui/components/BottomNavigationBar.kt`
- `ui/screens/progress/ProgressHubScreen.kt`
- Workout/program/Home ViewModel and screen files
- `data/home/*`, `data/workout/WorkoutRepository.kt`, `data/programs/ProgramRepository.kt`
- `WorkoutDao.kt`, `ProgramDao.kt`
- `NavigationContractTest.kt`, `HomeDashboardPeriodTest.kt`
- `NavigationSmokeTest.kt`, `HomeDashboardRepositoryTest.kt`
- lifecycle Compose and Compose UI test dependencies

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 8 tests, 0 failures.
- `connectedDebugAndroidTest` on Pixel 6 Pro API 34: 15 tests, 0 failures.
- UI coverage: five-target shell, nested Progress/back, selected-tab restoration, workout/program ID detail and Activity recreation.
- State coverage: three-source Home reactivity and historical Istanbul DST calendar boundary.
- `lintDebug`: BUILD SUCCESSFUL; 0 errors / 52 warnings.
- `assembleDebug`: BUILD SUCCESSFUL.
- Debug APK reinstalled/opened; five-target Home shell visually smoke-checked.

Risks changed:
- R6 program detail/purchase stale-state risk resolved.
- R7 unused workout route argument resolved.
- R13 seven-item phone navigation resolved; adaptive rail remains.
- R5 Home stale state mitigated; midnight rollover and loading/error remain.
- R8 historical workout edit/status semantics remains open.

Graph notes updated:
- [[ADR_002_FIVE_TARGET_NAVIGATION]]
- [[ARCH_NAVIGATION]], [[ARCH_STATE_DATA_FLOW]]
- [[FEATURE_HOME]], [[FEATURE_WORKOUT]], [[FEATURE_PROGRAMS]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]]

Next concrete action:
- PR 7: establish the Compose design-system foundation and modernize Home with reusable, accessible and adaptive components.

Blockers:
- None for PR 6. External deep links, navigation rail/list-detail and true process-death smoke remain follow-up acceptance items.

## 2026-07-15 - PR 7 design system and Home modernization

Completed:
- Added system-aware light/dark Material 3 palettes and matching light/dark platform launch themes.
- Added shared typography, spacing, shape, touch-target, icon and content-width tokens.
- Added reusable section, metric, quick-action, loading, empty and error Compose components.
- Rebuilt Home around a prominent workout CTA, explicit empty state, meaningful no-data labels, adaptive metrics, quick actions and last-workout summary.
- Replaced Home’s ambiguous initial zero state with `Loading/Content/Error`, retry and automatic local-midnight period renewal.
- Added nutrition entry count so “no meal” is distinguishable from a valid zero-calorie total.
- Moved Home and bottom-navigation user text to string resources.
- Made bottom navigation font-aware: at %200 font only the selected label is shown and labels remain single-line.

Files changed:
- `ui/theme/Theme.kt`, `ui/theme/DesignTokens.kt`
- `ui/components/GymTrackerComponents.kt`, `ui/components/BottomNavigationBar.kt`
- `ui/screens/home/HomeScreen.kt`, `HomeViewModel.kt`
- `data/home/HomeDashboardRepository.kt`, `ui/navigation/NavGraph.kt`
- `res/values/strings.xml`, `res/values/themes.xml`, `res/values-night/themes.xml`
- `HomeLayoutConfigTest.kt`, `HomeScreenTest.kt`, `HomeDashboardRepositoryTest.kt`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 11 tests, 0 failures.
- `connectedDebugAndroidTest`: BUILD SUCCESSFUL; 18 tests, 0 failures on Pixel 6 Pro API 34.
- Home UI coverage: light/dark composition, %200 font, empty/content/error, retry, no fake zeroes and merged metric semantics.
- `lintDebug`: BUILD SUCCESSFUL; 0 errors / 52 warnings.
- `assembleDebug`: BUILD SUCCESSFUL.
- Visual QA passed for `pr7-home-light.png`, `pr7-home-dark.png` and `pr7-home-font-200.png`; debug APK reinstalled, font scale restored to 1.0 and app reopened in dark mode.

Risks changed:
- R5 is strongly mitigated: reactivity, midnight renewal and loading/error separation are implemented; Clock/zone-change injection test remains.
- R15 is partial: Home/navigation resource, semantics and %200 font work is complete; the remaining screens still need migration.
- No Room schema change was made in PR7.

Graph notes updated:
- [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]], [[MOC_DESIGN]]
- [[FEATURE_HOME]], [[ARCH_STATE_DATA_FLOW]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- PR 8: define workout ACTIVE/COMPLETED semantics with an explicit Room migration, enforce one resumable active session, modernize the critical workout flow and connect Home’s CTA to it.

Blockers:
- None. Navigation rail, permanent screenshot-golden infrastructure, Clock injection and true process-death smoke remain explicit follow-up work.

## 2026-07-15 - PR 8 workout lifecycle and modernization

Completed:
- Added [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] and Room v6 `ACTIVE/COMPLETED` + `completedAt` contract.
- Added explicit `MIGRATION_5_6`; legacy sessions are preserved as `COMPLETED`, never guessed to be active.
- Added transactional start-or-resume and status-conditional add/delete/finish operations; completed sessions reject mutation.
- Split active and completed Flow state through repository/ViewModel and exposed explicit mutation errors.
- Connected Home CTA to the active session ID while excluding active records from completed metrics.
- Rebuilt Workout list/detail with PR7 tokens, active/history hierarchy, read-only completed detail and large-font adaptive rows/headers.

Files changed:
- `Entities.kt`, `AppDatabase.kt`, `DatabaseMigrations.kt`, `WorkoutDao.kt`, v6 schema JSON
- `WorkoutRepository.kt`, `WorkoutViewModel.kt`, `WorkoutScreen.kt`
- `HomeDashboardRepository.kt`, `HomeScreen.kt`, `strings.xml`
- `DatabaseMigrationTest.kt`, `HomeDashboardRepositoryTest.kt`, `WorkoutRepositoryTest.kt`, `WorkoutScreenTest.kt`

Tests run/results:
- `testDebugUnitTest`: BUILD SUCCESSFUL; 11 tests, 0 failures.
- `connectedDebugAndroidTest`: BUILD SUCCESSFUL; 23 tests, 0 failures on Pixel 6 Pro API 34.
- `lintDebug`: BUILD SUCCESSFUL; 0 errors / 53 warnings.
- `assembleDebug`: BUILD SUCCESSFUL.
- Manual API 34 visual QA: active light, active dark at %200 font, finish dialog at %200 font and completed read-only detail. The first completed-list screenshot exposed a cramped status row; final code switches list cards and detail headers to vertical large-font layouts and the 23-test quality gate was rerun successfully.

Risks changed:
- R8 resolved: legacy sessions migrate to completed and historical details are read-only.
- R11 partial: Workout set/finish validation is enforced; other features remain.
- R15 partial+: Home/navigation/Workout now cover resources, adaptive %200 font and light/dark; Nutrition/Measurements are next.
- Remaining Workout limitation: single-active is guaranteed by the app’s transactional write path, not a partial unique DB index; revisit if multi-process or external writers are introduced.

Graph notes updated:
- [[ADR_006_WORKOUT_SESSION_LIFECYCLE]], [[ADR_INDEX]]
- [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- Start PR9 with [[FEATURE_NUTRITION]] and [[FEATURE_MEASUREMENTS]] source_files plus their Clock/validation/change-impact obligations.

Blockers:
- None. True OS process-death smoke, permanent screenshot-golden infrastructure and DB-level single-active uniqueness remain explicit follow-ups.

## 2026-07-15 - PR 9 Nutrition + Measurements correctness and modernization

Completed:
- Added shared finite comma/dot decimal parsing and explicit Nutrition/Measurement validation rules.
- Replaced fixed 86,400,000 ms Nutrition boundaries with `LocalDate + Clock + ZoneId`; spring/autumn DST days are covered.
- Persisted selected Nutrition date through `SavedStateHandle`, added previous/next/today navigation and preserved meaningful entry timestamps.
- Prevented zero/negative goals and invalid food/amount writes; exposed finite raw progress so over-goal state is visible.
- Prevented empty/unrealistic measurement rows, separated latest from history and added newest-two weight delta.
- Rebuilt both screens with shared tokens, resources, empty/error states and adaptive normal/%200 font layouts.

Files changed:
- `domain/LocalizedDecimal.kt`, `di/TimeModule.kt`
- `data/nutrition/NutritionRules.kt`, Nutrition Screen/ViewModel
- `data/measurements/MeasurementRules.kt`, Measurements Screen/ViewModel
- Nutrition/Measurement JVM and Compose tests, `strings.xml`
- Related Obsidian feature/architecture/design/quality/roadmap/workflow nodes

Tests run/results:
- `testDebugUnitTest`: 17 tests, 0 failures.
- `connectedDebugAndroidTest`: 27 tests, 0 failures on Pixel 6 Pro API 34.
- `lintDebug`: 0 errors / 63 warnings.
- `assembleDebug`: success.
- Manual light normal-font Nutrition and dark %200 Nutrition/Measurements screenshots passed after fixing macro card width/adaptive layout.

Risks changed:
- R9 resolved: DST day range, zero-goal and over-goal behavior are deterministic and tested.
- R11 partial+: Workout, Nutrition and Measurements now validate inputs before mutation.
- R15 partial++: token/resource/light-dark/%200 coverage now includes Nutrition and Measurements.
- No Room schema change in PR9; measurement date index remains coupled to the later chart/query slice.

Graph notes updated:
- [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- Keep draft PR #2 stacked on PR #1 during review, then start PR10 with [[FEATURE_PROGRESS_PHOTOS]] + [[FEATURE_MUSCLE_LOAD]].

Blockers:
- No code blocker. Draft PR #2 is stacked on draft PR #1 (`codex/workout-lifecycle`) and must keep that base until PR8 lands or be rebased afterward.

## 2026-07-15 - PR 10 Progress Photos + Muscle Load modernization

Completed:
- Added typed progress-photo validation, app-owned storage reconciliation and device-local backup/transfer exclusions while preserving legacy `storageVersion=0` rows.
- Rebuilt the Progress hub and photo flow with adaptive grids, empty/error states, full-screen viewing, legacy indicators and accessible normal/%200-font behavior.
- Replaced the old fatigue calculation with a completed-set-only, reactive seven-day Muscle Load model using recency weighting, bodyweight fallback and unknown-exercise reporting.
- Added methodology, front/back body-map controls, color-independent load labels and accessible score summaries.

Files changed:
- `data/photos/ProgressPhotoRules.kt`, `PhotoFileStorage.kt`, `ProgressPhotoRepository.kt`
- `data/workout/CompletedSetForLoad.kt`, `WorkoutDao.kt`, `domain/MuscleLoadCalculator.kt`
- Progress hub/photo and Muscle Load Screen/ViewModel/component files
- Manifest backup configuration, `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`, strings and related tests
- Related Obsidian feature/ADR/architecture/design/quality/roadmap/workflow nodes

Tests run/results:
- `testDebugUnitTest`: 20 tests, 0 failures.
- `connectedDebugAndroidTest`: 33 tests, 0 failures on Pixel 6 Pro API 34.
- `lintDebug`: 0 errors / 67 warnings.
- `assembleDebug`: success.
- Manual light/dark/%200-font visual acceptance passed for the Progress hub, Progress Photos and Muscle Load screens.

Risks changed:
- R2 is further mitigated by missing-row/orphan reconciliation; legacy provider-backed rows remain visible and explicitly marked.
- R10 core ambiguity is resolved: the feature is named Muscle Load and its current completed-set formula is documented and tested.
- R14 is further mitigated by explicitly excluding managed progress photos from Android cloud backup and device transfer.
- R15 is partial+++: Progress hub, photos and Muscle Load now use shared tokens, resources, accessibility semantics and large-font adaptations.
- No Room schema change was made in PR10.

Graph notes updated:
- [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]]
- [[ADR_004_PERSISTENT_PHOTO_STORAGE]], [[ADR_005_MUSCLE_LOAD_NAMING]]
- [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- Review, commit and push the uncommitted `codex/progress-load` working tree, then open a stacked draft PR targeting `codex/nutrition-measurements`.
- After publication, start PR11 from [[FEATURE_PROGRAMS]] with the stable exercise-ID/relational model decision before UI work.

Blockers:
- No code blocker. PR10 is intentionally uncommitted and stacked on PR #2 until the user requests publication.

## 2026-07-15 - PR 11 Programs normalization and workout integration

Completed:
- Added Room v7 `ProgramDayExercise` with ordered relational targets, stable exercise IDs and explicit `MIGRATION_6_7`.
- Migrated valid v6 JSON to relational rows while preserving malformed legacy payloads without crashing or silent deletion.
- Added atomic locked/ownership/target-validated program-day→ACTIVE-workout copy; existing active sessions resume without duplicate sets.
- Added planned WorkoutSet semantics (`exerciseId`, `targetReps`, `exerciseOrder`, incomplete state), preserved prescription order, real reps/kg completion and finish blocking until planned sets are completed or deleted.
- Rebuilt Programs list/detail with resource/token-based adaptive UI, demo-access language, relational plan cards and accessible light/dark/%200-font behavior.

Files changed:
- Room entities, AppDatabase v7, `MIGRATION_6_7`, schema 7 JSON, Program/Workout DAOs and repositories
- `ProgramWorkoutPlan.kt`, stable ID/legacy serializer domain model and versioned seed writer
- Programs and Workout Screen/ViewModel integration, MainActivity navigation callback and strings/plurals
- Migration/seed/repository/Compose/navigation tests and related Obsidian nodes

Tests run/results:
- `testDebugUnitTest`: 22 tests, 0 failures.
- `connectedDebugAndroidTest`: 41 tests, 0 failures on Pixel 6 Pro API 34 from a clean package state.
- `lintDebug`: 0 errors / 71 warnings after the final modifier-order cleanup.
- `assembleDebug`: success.
- Manual visual acceptance passed for light Programs/detail and dark %200 Programs/detail/planned-workout screens.

Risks changed:
- R12 core resolved: active program reads are relational and malformed JSON no longer crashes UI; legacy payload removal remains a later evidence-backed migration.
- R11 partial++: Programs locked/ownership/target and planned-set mutations are validated at repository/transaction boundaries.
- R15 mitigated-core: all current core feature surfaces now have resource/token/light-dark/%200-font coverage; app-wide adaptive/golden work remains.
- New schema v7 is explicit and tested from v3 and v6; no v1/v2 support claim is made.

Graph notes updated:
- [[FEATURE_PROGRAMS]], [[ADR_007_RELATIONAL_PROGRAM_PLANS]], [[ADR_INDEX]]
- [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- PR11 `7917358` + docs follow-up ile stacked draft PR #4 olarak yayınlandı.
- Start PR12 Nutrition/Measurements edit-undo-chart and second-locale work without waiting for an additional prompt.

Blockers:
- No code blocker. PR #4 remains stacked on PR #3 until its base lands or is rebased.

## 2026-07-15 - PR 12 Nutrition/Measurements depth

Completed:
- Added Room v8 `body_measurements(date)` index, explicit `MIGRATION_7_8`, exported schema and v7 fixture/index preservation test.
- Added `MeasurementRepository`, indexed 7/30/90/all period Flow, SavedState chart selection and real accessible weight chart.
- Added measurement custom date, edit, latest/history delete and same-ID Snackbar undo.
- Added Nutrition snapshot-safe amount/meal edit and same-ID delete undo; FoodEntry history never rereads mutable FoodItem macros.
- Added scoped English Nutrition/Measurements resources, locale date formatting, plural chart count and language-independent UI assertions.
- Added [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]].

Files changed:
- Room entity/DAO/database/migration + schema v8
- Nutrition/Measurement rules, repository, ViewModels, Screens and TR/EN resources
- Migration, mutation, Home reactivity, locale, rules and Compose tests
- Related Obsidian feature/ADR/architecture/design/quality/roadmap/workflow nodes

Tests run/results:
- `testDebugUnitTest`: 24 tests, 0 failures.
- `connectedDebugAndroidTest`: 46 tests, 0 failures on a fresh Pixel 6 Pro API 34 emulator.
- `lintDebug`: 0 errors / 77 warnings.
- `assembleDebug`: success.
- Manual light normal-font and dark %200-font visual acceptance passed for Nutrition, Measurements and the real chart.

Risks changed:
- R11 mitigated-core: edit/delete/undo identity and FoodEntry snapshot rules are explicit and tested.
- R15 mitigated-core+: Nutrition/Measurements add scoped English, plural/unit formatting and language-independent tests; full-app locale/golden remains.
- Room v8 is explicit and tested from v3, v6 and v7; no v1/v2 support claim is made.

Graph notes updated:
- [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]], [[ADR_INDEX]]
- [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]], [[SCREEN_BLUEPRINT_GRAPH]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- PR12 `9c90471` stacked draft PR #5 olarak yayınlandı.
- Stop and wait for the user; do not start PR13 without a new command.

Blockers:
- No code blocker. PR12 remains intentionally stacked on PR #4 until its base lands or is rebased.

## 2026-07-16 - PR 13 Photo media pipeline + personal load calibration

Completed:
- Added Room v9 `ProgressPhoto.thumbnailUri/widthPx/heightPx`, explicit `MIGRATION_8_9` and exported schema.
- Added bounded real-image decode, EXIF orientation normalization, max-2048 main JPEG and max-512 grid thumbnail generation.
- Made main/thumbnail publish, DB rollback, delete and reconciliation contracts two-file safe; missing thumbnail falls back to the main image.
- Combined completed-set Flow with the newest valid body measurement; zero-weight sets use personal weight and visibly fall back to 20 kg when unavailable.
- Updated Muscle Load methodology and summary UI to disclose the active calibration source.

Files changed:
- Photo entity/DAO/database/migration/schema, `PhotoFileStorage`, repository, Progress grid and photo tests.
- `MeasurementDao`, `MuscleLoadCalculator`, Fatigue ViewModel/Screen, strings and calibration tests.
- Related Obsidian feature/ADR/architecture/quality/roadmap/workflow nodes.

Tests run/results:
- `testDebugUnitTest`: 26 tests, 0 failures.
- `connectedDebugAndroidTest`: 50 tests, 0 failures on Pixel 6 Pro API 34.
- Targeted photo/migration/mutation/Muscle Load device suite: 16 tests, 0 failures after the final reconciliation change.
- `lintDebug`: 0 errors / 78 warnings.
- `assembleDebug`: success.

Risks changed:
- R2 mitigated-core: new imports no longer retain provider orientation, unbounded source dimensions or full-size grid decode; corrupt input and two-file rollback are tested.
- R10 mitigated-core+: personal bodyweight calibration replaces the fixed fallback when a valid measurement exists; stable-ID load projection and per-movement coefficients remain.
- Room v9 is explicit and tested from v3, v6, v7 and v8; no v1/v2 support claim is made.

Graph notes updated:
- [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]]
- [[ADR_004_PERSISTENT_PHOTO_STORAGE]], [[ADR_005_MUSCLE_LOAD_NAMING]], [[ADR_INDEX]]
- [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- PR13 stacked draft PR #6 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/6
- Stop and wait for the user before starting the app-wide locale/adaptive/golden slice.

Blockers:
- No code blocker. PR13 is intentionally stacked on PR #5 until its base lands or is rebased.

## 2026-07-16 - PR 14 App-wide locale + adaptive navigation + golden baseline

Completed:
- Expanded English resources from scoped coverage to exact app-wide parity with the Turkish default catalog.
- Localized all 32 Muscle Load display names and made Programs currency formatting follow the active configuration locale.
- Added a compact bottom-bar / 600 dp+ navigation-rail shell with shared top-level selection and save/restore behavior.
- Added merged button semantics and stable test tags to Progress Hub destinations; decorative navigation/chevron icons no longer duplicate TalkBack labels.
- Added permanent pixel-golden infrastructure with English light and Turkish dark/%200 Progress Hub baselines.
- Reworked legacy Compose assertions to consume active-locale resources and made workout route smoke repeatable with or without a pre-existing active session.

Files changed:
- `MainActivity.kt`, `BottomNavigationBar.kt`, `ProgressHubScreen.kt`, `FatigueScreen.kt`, `ProgramsScreen.kt`, `WorkoutScreen.kt`
- `res/values/strings.xml`, `res/values-en/strings.xml`
- Resource coverage, adaptive navigation, localization, Progress Hub semantics, golden and existing language-independent Compose tests
- Related navigation/ADR/design/quality/risk/roadmap/workflow Obsidian nodes

Tests run/results:
- `testDebugUnitTest`: 28 tests, 0 failures.
- `connectedDebugAndroidTest`: 54 tests, 0 failures on Pixel 6 Pro API 34, including both golden comparisons.
- `lintDebug`: success, 0 errors.
- `assembleDebug`: success.
- Resource contract: default 350, English 350, key diff 0.

Risks changed:
- R13 resolved-core: compact five-target navigation and 600 dp+ rail now share the same ownership and state contract; feature list-detail remains enhancement work.
- R15 mitigated+: full TR/EN parity, locale-aware display values, adaptive shell, accessibility semantics and the first permanent golden baseline are active.

Graph notes updated:
- [[ARCH_NAVIGATION]], [[ADR_002_FIVE_TARGET_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]]
- [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- PR14 stacked draft PR #7 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/7
- Stop and wait for the user before starting PR15.

Blockers:
- No code blocker. CI, external deep links, true OS process-death, feature list-detail and broader golden coverage remain explicit follow-ups.

## 2026-07-16 - PR 15 CI + minified release hardening

Completed:
- Added a Linux-executable Gradle wrapper and root `ciQuality` task as the single local/hosted JVM, lint, debug and release gate.
- Added GitHub Actions quality and dependent API 34 Pixel 6 Pro device/golden jobs with concurrency cancellation and read-only permissions.
- Added 14-day lint, device-report, unsigned APK/AAB and R8 mapping artifacts.
- Enabled R8 code minification and resource shrinking without broad project keep rules.
- Added minified release APK and AAB production to the same quality contract; production signing secrets remain outside the repository.
- Enabled hosted-runner KVM access after the first run exposed software-emulation APK install failure.
- Stabilized the %200 font Measurements UI test with lazy-list container scrolling after the second hosted run exposed an offscreen-node assumption.
- Published stacked draft PR #8 and completed hosted run `29459249716` with both jobs green.

Files changed:
- `.github/workflows/android-ci.yml`, `gradlew`
- root/app Gradle build files and `app/proguard-rules.pro`
- Related build, quality, risk, roadmap, atlas, impact, start and handoff Obsidian nodes

Tests run/results:
- `ciQuality`: successful; 28 JVM tests, lint, debug APK, release APK, release AAB and mapping.
- `connectedDebugAndroidTest`: 54 tests, 0 failures on Pixel 6 Pro API 34, including goldens.
- Hosted GitHub Actions run `29459249716`: quality 8m51s; API 34 device/golden 13m13s; report artifact 54 tests, 0 failures/errors/skipped.
- Minified unsigned APK: 2,556,733 bytes versus 19,981,034-byte debug APK; release AAB: 4,809,602 bytes.
- Release manifest inspection: package `com.gymtracker`, min SDK 26, target/compile SDK 34.

Risks changed:
- R3 resolved-core: local and hosted CI/release/device gates are green; making checks mandatory through branch protection remains a repository administration follow-up.
- R16 added partial: production signing, keystore secret policy and store-distribution smoke are intentionally open.

Graph notes updated:
- [[ARCH_BUILD_DI]], [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]]
- [[SOURCE_FILE_ATLAS]], [[CHANGE_IMPACT_MAP]], [[SESSION_HANDOFF]], [[00_START_HERE]]

Next concrete action:
- Stop and wait for the user before starting PR16 production signing/distribution or performance work.

Blockers:
- No local code blocker. Production signing requires an authorized keystore/secret decision and is outside this PR.
