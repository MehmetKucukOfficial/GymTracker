---
type: quality
status: current
implementation_status: mixed
verified_commit: e1359bb
updated: 2026-07-20
tags: [gymtracker/quality, gymtracker/testing]
---

# Test Matrisi

## Mevcut temel (2026-07-20)

- JVM unit test altyapisi `JUnit 4` ile aktiftir; `testDebugUnitTest` artik `NO-SOURCE` degildir.
- [[FatigueCalculatorTest]] bilinen egzersiz aktivasyon dagilimini, cap davranisini ve bilinmeyen egzersiz davranisini korur.
- Room instrumentation test altyapisi AndroidJUnitRunner ve `room-testing` ile aktiftir.
- [[WorkoutDaoTest]] tarih sinirli set sorgusunu ve session -> set cascade davranisini cihaz/emulator calistirmasi icin tanimlar.
- [[DatabaseMigrationTest]] v3→v9 satır korumasını; legacy workout/photo varsayılanlarını; v6 program JSON→ilişkisel satır dönüşümünü; bozuk JSON fallback’ini, yeni WorkoutSet kolonlarını, v7 measurement date index’ini ve v8→v9 photo media metadata varsayılanlarını doğrular.
- [[ProgramSeedInitializerTest]] tekrarlı, eşzamanlı ve legacy seed durumlarını kapsar.
- [[ProgressPhotoRepositoryTest]] app-owned EXIF dönüşümü, 2048 ana/512 thumbnail, dimension metadata, storage yeniden oluşturma, iki dosyalı delete/DB rollback, bozuk decode, 25 MB sınırı ve orphan/missing-main/missing-thumbnail reconciliation’ını kapsar.
- [[NavigationContractTest]] 5 hedef sırasını, nested route sahipliğini, detail görünürlük politikasını ve pozitif ID kuralını; `DeepLinkContractTest` on dış URI pattern’ini, trusted scheme/host ve bozuk ID reddini doğrular.
- [[HomeDashboardPeriodTest]] Istanbul takvim/DST gün sınırını; [[HomeDashboardRepositoryTest]] completed/active workout, measurement ve nutrition Flow’larının reaktif birleşimini doğrular.
- [[NavigationSmokeTest]] ortak top bar back, sistem back, selected tab, workout/program ID detail ve Activity recreation davranışını; `DeepLinkNavigationTest` manifest çözümü, cold-start progress child, pozitif program detail, invalid-ID safe fallback ve recreation davranışını gerçek Compose UI üzerinde doğrular.
- `ProcessDeathNavigationTest`, hedef uygulamadan ayrı instrumentation sürecinde Progress Photos ve pozitif-ID Program detail görevini arka plana alır; `am kill` ile eski PID’nin yok olduğunu, aynı task öne getirildiğinde yeni PID ve doğru destination’ın geri geldiğini doğrular.
- `HomeLayoutConfigTest` compact/expanded ve %200 font kolon politikasını JVM’de doğrular.
- `HomeScreenTest` light/dark render, %200 font empty state, sahte sıfır yokluğu, birleşik metric semantics ve error retry davranışını doğrular.
- `WorkoutRepositoryTest` eşzamanlı start/resume çağrılarının tek aktif ID üretmesini ve tamamlanan seansın mutation’lara kapanmasını doğrular.
- `WorkoutScreenTest` aktif/geçmiş ayrımını, tamamlanan detayın salt okunurluğunu ve light/dark + %200 font yerleşimini doğrular.
- `LocalizedDecimalTest`, `NutritionRulesTest` ve `MeasurementRulesTest` virgül/nokta parse, DST 23/25 saat, zero/over-goal, snapshot ölçekleme, input aralıkları, chart period sınırı ve en yeni iki kilo trendini doğrular.
- `MutationContractTest` Nutrition/Measurement update-delete-aynı-ID restore ve indexed period query’yi; `HomeDashboardRepositoryTest` edit sonrası Home reaktivitesini doğrular.
- `NutritionScreenTest` over-goal/empty state’i; `MeasurementsScreenTest` latest/history, gerçek chart semantics ve period action’ını dark + %200 font koşulunda doğrular.
- `ResourceCoverageTest` Türkçe/İngilizce string ve plural anahtarlarının birebir eşitliğini; `LocalizationResourcesTest` bütün ana feature ailelerinin English karşılığını doğrular.
- `AdaptiveNavigationTest` 599/600/840 dp sınırını; `NavigationSmokeTest` gerçek compact bottom bar ve expanded rail seçimini doğrular.
- `GtListDetailLayoutTest` 839/840/1200 dp liste–detay sınırını; Programs/Workout Compose testleri expanded seçim, kararlı-ID açık eylemi ve Programs saved-state restoration davranışını doğrular.
- `ProgressHubScreenTest` %200 fontta birleşik button semantics/erişilebilirliği; `ProgressHubGoldenTest` English light ve Turkish dark/%200 pixel baseline’larını doğrular.
- `AdaptiveListDetailGoldenTest` 1000×700 English/light Programs ve Turkish/dark/%130 Workout liste–özet pixel baseline’larını doğrular.
- `GtFeedbackComponentsTest`, `%200` fontta ortak top bar başlık/support metni, tek yerelleştirilmiş geri eylemi ve ortak Snackbar mesaj/undo semantics’ini doğrular. `CriticalStateGoldenTest`, Turkish dark/%200 Nutrition over-goal ile English/light Measurements chart + top bar baselinelarını doğrular; kalıcı golden toplamı altıdır.
- `ProgressPhotoRulesTest`, `ProgressPhotoRepositoryTest` ve `ProgressPhotoScreenTest` fotoğraf input aralığı, import/rollback/limit, orphan/missing reconciliation, v0 yeniden seçimde kimlik/metadata koruma, v1 eski dosya temizliği, update rollback ve light/dark/%200 font yükseltme eylemlerini doğrular.
- `MuscleLoadCalculatorTest`, `WorkoutDaoTest`, `MutationContractTest` ve `MuscleLoadScreenTest` recency/personal-bodyweight/fallback/invalid ölçüm/unknown/cap kurallarını, completed-only + latest-valid-weight reaktif query’lerini ve kalibrasyon kaynağı gösteren renk bağımsız adaptive UI’ı doğrular.
- `ExerciseIdentityTest`, `ProgramSeedInitializerTest`, `ProgramRepositoryTest`, `WorkoutRepositoryTest` ve `ProgramsScreenTest`; unique/locale-independent ID, 5/23/108 seed, locked guard, atomic plan copy, incomplete-set finish engeli ve adaptive UI’yı doğrular.
- Pixel 6 Pro API 34 emülatöründe 71 instrumentation testi ve 34 JVM testi, 0 failure ile geçer.
- Ayrı `:baselineprofile` benchmarkRelease kabul paketinde 2 gerçek süreç-ölümü testi, 0 failure ile geçer.
- GitHub Actions koşusu `29459249716`: quality 8 dk 51 sn; KVM hızlandırmalı API 34 device/golden 13 dk 13 sn; artifact XML toplamı 54 test, 0 failure/error/skipped.
- PR16: 2 Baseline Profile üretici testi sıfır hatayla release baseline/startup profili yazdı; startup None/Partial, Home scroll ve Workout macrobenchmark senaryoları API 34 emülatörde çalıştı. Fiziksel cihaz performans kabulü açık.
- PR16 hosted GitHub Actions koşusu `29473550009`: quality ve API 34 device/golden işleri tamamen yeşil.
- PR17 hosted GitHub Actions koşusu `29474876318`: quality 11m08s; API 34 device/golden 14m06s; artifact XML 58 test, 0 failure/error/skipped.
- PR #11 post-merge bakım koşusu `29497592820`: quality 10m49s; API 34 device/golden 13m23s; artifact XML 58 test, 0 failure/error/skipped.
- Otomatik `master` kabul koşusu `29499237460` (`3f2f8cf`): quality 11m54s; API 34 device/golden 9m26s; artifact XML 58 test, 0 failure/error/skipped.
- PR18/GitHub PR #12 hosted koşusu `29503247012`: quality 7m23s; API 34 device/golden 9m41s; artifact XML 62 test, 0 failure/error/skipped.
- PR18 merge-commit master kabul koşusu `29504722658`: quality 8m10s; API 34 device/golden 10m14s; artifact XML 62 test, 0 failure/error/skipped.
- PR20 merge-commit `625489a` otomatik master kabul koşusu `29762392465`: quality ve API 34 device/process-death işleri yeşil; indirilen artifact XML 66 app + 2 ayrı process-death testi, 0 failure/error/skipped gösterir.
- `releaseDistribution` dummy upload keystore ile APK v2/v3, AAB JAR imzası ve embedded profile girdilerini doğruladı; secretsiz çağrı dört eksik environment değerini fail-fast raporladı.

## Feature matrisi

| Alan | Unit | Room/instrumentation | Compose/UI | Smoke |
|---|---|---|---|---|
| Home | aggregate + clock + adaptive policy | summary queries | empty/content/error, semantics, light/dark, %200 font | dashboard açılış |
| Workout | state/validation + 839/840 layout | v3→v7, transaction/cascade/single-active/read-only/planned set | active/history/planned completion, expanded list-detail, golden, light/dark, %200 font | external positive-ID deep link + recreation |
| Nutrition | totals/DST/parser/goal/snapshot scale | day query/update/delete/same-ID undo | over-goal/empty/edit action/ortak Snackbar/dark/%200/English + golden | gerçek process-death bekliyor |
| Measurements | validation/trend/period | v3→v8, date index/range/update/delete/same-ID undo | latest/history/chart/period/empty/ortak top bar+Snackbar/dark/%200/English + golden | process-death/timezone bekliyor |
| Photos | input/limit/media bounds/backup policy | v3→v9 + v8→v9/import/EXIF/thumbnail/delete/reconcile/legacy-upgrade rollback | thumbnail grid/v0 reselect/v1 optimize/viewer/ortak top bar+Snackbar/dark/%200 | alt rota gerçek process-death; reboot/low-storage bekliyor |
| Programs | stable ID/content + 839/840 layout | idempotent 5/23/108 seed, v6→v7 JSON migration, atomic plan copy | list/detail/locked/start, ortak top bar+Snackbar, expanded selection/restoration, golden, dark/%200 | positive-ID detail Activity recreation + gerçek process-death |
| Muscle load | activation/decay/personal bodyweight/fallback/unknown | completed-only set + latest-valid-weight Flow | map/list/calibration/methodology/dark/%200 | midnight + TalkBack bekliyor |

## Room ve migration

- v3→latest (v9) doğrulandı; v1/v2 tarihi şema kanıtı olmadığı için destek iddiası yok
- Seed repeated/concurrent
- Transaction rollback
- Cascade and unique constraint
- Date indexes/query boundaries
- Realistic fixture row preservation

## ViewModel ve state

- `runTest` + test dispatcher
- Flow assertions/Turbine
- Loading→content/error
- SavedStateHandle route ID
- Event consumption/undo
- Clock injection

Home’un takvim sınırı saf fonksiyonla testlidir; canlı rollover akışına Clock/ZoneId injection henüz eklenmedi. Nutrition Clock kullanır ve seçili tarihi SavedStateHandle’da korur; gerçek OS process-death kabulü bekler.

## CI sırası

`ciQuality`: unit → lintDebug → debug APK → minified/resource-shrunk release APK + AAB + mapping + embedded Baseline Profile doğrulaması. Pull request ve `master` push’unda quality job’u geçtikten sonra API 34 Pixel 6 Pro üzerinde 71 Room/Compose/navigation/golden cihaz testi ve ayrı benchmarkRelease sürecinde 2 gerçek process-death testi çalışır. Her iki modülün device raporları artifact olarak saklanır. Production signing manuel protected-environment kapısıdır; fiziksel cihaz benchmark ve Play internal-track smoke’u ayrıca gerekir.

Definition of Done ayrıntısı: [[GELISTIRME_MASTER_PLANI#Definition of Done]].
