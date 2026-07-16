---
type: quality
status: current
implementation_status: mixed
verified_commit: abd3fbe
updated: 2026-07-16
tags: [gymtracker/quality, gymtracker/testing]
---

# Test Matrisi

## Mevcut temel (2026-07-16)

- JVM unit test altyapisi `JUnit 4` ile aktiftir; `testDebugUnitTest` artik `NO-SOURCE` degildir.
- [[FatigueCalculatorTest]] bilinen egzersiz aktivasyon dagilimini, cap davranisini ve bilinmeyen egzersiz davranisini korur.
- Room instrumentation test altyapisi AndroidJUnitRunner ve `room-testing` ile aktiftir.
- [[WorkoutDaoTest]] tarih sinirli set sorgusunu ve session -> set cascade davranisini cihaz/emulator calistirmasi icin tanimlar.
- [[DatabaseMigrationTest]] v3→v9 satır korumasını; legacy workout/photo varsayılanlarını; v6 program JSON→ilişkisel satır dönüşümünü; bozuk JSON fallback’ini, yeni WorkoutSet kolonlarını, v7 measurement date index’ini ve v8→v9 photo media metadata varsayılanlarını doğrular.
- [[ProgramSeedInitializerTest]] tekrarlı, eşzamanlı ve legacy seed durumlarını kapsar.
- [[ProgressPhotoRepositoryTest]] app-owned EXIF dönüşümü, 2048 ana/512 thumbnail, dimension metadata, storage yeniden oluşturma, iki dosyalı delete/DB rollback, bozuk decode, 25 MB sınırı ve orphan/missing-main/missing-thumbnail reconciliation’ını kapsar.
- [[NavigationContractTest]] 5 hedef sırasını, nested route sahipliğini, detail görünürlük politikasını ve pozitif ID kuralını doğrular.
- [[HomeDashboardPeriodTest]] Istanbul takvim/DST gün sınırını; [[HomeDashboardRepositoryTest]] completed/active workout, measurement ve nutrition Flow’larının reaktif birleşimini doğrular.
- [[NavigationSmokeTest]] back, selected tab, workout/program ID detail ve Activity recreation davranışını gerçek Compose UI üzerinde doğrular.
- `HomeLayoutConfigTest` compact/expanded ve %200 font kolon politikasını JVM’de doğrular.
- `HomeScreenTest` light/dark render, %200 font empty state, sahte sıfır yokluğu, birleşik metric semantics ve error retry davranışını doğrular.
- `WorkoutRepositoryTest` eşzamanlı start/resume çağrılarının tek aktif ID üretmesini ve tamamlanan seansın mutation’lara kapanmasını doğrular.
- `WorkoutScreenTest` aktif/geçmiş ayrımını, tamamlanan detayın salt okunurluğunu ve light/dark + %200 font yerleşimini doğrular.
- `LocalizedDecimalTest`, `NutritionRulesTest` ve `MeasurementRulesTest` virgül/nokta parse, DST 23/25 saat, zero/over-goal, snapshot ölçekleme, input aralıkları, chart period sınırı ve en yeni iki kilo trendini doğrular.
- `MutationContractTest` Nutrition/Measurement update-delete-aynı-ID restore ve indexed period query’yi; `HomeDashboardRepositoryTest` edit sonrası Home reaktivitesini doğrular.
- `NutritionScreenTest` over-goal/empty state’i; `MeasurementsScreenTest` latest/history, gerçek chart semantics ve period action’ını dark + %200 font koşulunda doğrular.
- `ResourceCoverageTest` Türkçe/İngilizce string ve plural anahtarlarının birebir eşitliğini; `LocalizationResourcesTest` bütün ana feature ailelerinin English karşılığını doğrular.
- `AdaptiveNavigationTest` 599/600/840 dp sınırını; `NavigationSmokeTest` gerçek compact bottom bar ve expanded rail seçimini doğrular.
- `ProgressHubScreenTest` %200 fontta birleşik button semantics/erişilebilirliği; `ProgressHubGoldenTest` English light ve Turkish dark/%200 pixel baseline’larını doğrular.
- `ProgressPhotoRulesTest`, `ProgressPhotoRepositoryTest` ve `ProgressPhotoScreenTest` fotoğraf input aralığı, import/rollback/limit, orphan/missing reconciliation, legacy badge ve %200 font empty state’ini doğrular.
- `MuscleLoadCalculatorTest`, `WorkoutDaoTest`, `MutationContractTest` ve `MuscleLoadScreenTest` recency/personal-bodyweight/fallback/invalid ölçüm/unknown/cap kurallarını, completed-only + latest-valid-weight reaktif query’lerini ve kalibrasyon kaynağı gösteren renk bağımsız adaptive UI’ı doğrular.
- `ExerciseIdentityTest`, `ProgramSeedInitializerTest`, `ProgramRepositoryTest`, `WorkoutRepositoryTest` ve `ProgramsScreenTest`; unique/locale-independent ID, 5/23/108 seed, locked guard, atomic plan copy, incomplete-set finish engeli ve adaptive UI’yı doğrular.
- Pixel 6 Pro API 34 emülatöründe 54 instrumentation testi ve 28 JVM testi, 0 failure ile geçer.
- GitHub Actions koşusu `29459249716`: quality 8 dk 51 sn; KVM hızlandırmalı API 34 device/golden 13 dk 13 sn; artifact XML toplamı 54 test, 0 failure/error/skipped.

## Feature matrisi

| Alan | Unit | Room/instrumentation | Compose/UI | Smoke |
|---|---|---|---|---|
| Home | aggregate + clock + adaptive policy | summary queries | empty/content/error, semantics, light/dark, %200 font | dashboard açılış |
| Workout | state/validation | v3→v7, transaction/cascade/single-active/read-only/planned set | active/history/planned completion, light/dark, %200 font | Activity recreation; process death bekliyor |
| Nutrition | totals/DST/parser/goal/snapshot scale | day query/update/delete/same-ID undo | over-goal/empty/edit action/dark/%200/English | gerçek process-death bekliyor |
| Measurements | validation/trend/period | v3→v8, date index/range/update/delete/same-ID undo | latest/history/chart/period/empty/dark/%200/English | process-death/timezone bekliyor |
| Photos | input/limit/media bounds/backup policy | v3→v9 + v8→v9/import/EXIF/thumbnail/delete/reconcile rollback | thumbnail grid/legacy/viewer/dark/%200 | process kill/restart/reboot/low-storage bekliyor |
| Programs | stable ID/content | idempotent 5/23/108 seed, v6→v7 JSON migration, atomic plan copy | list/detail/locked/start, dark/%200 | program→workout + Activity recreation |
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

`ciQuality`: unit → lintDebug → debug APK → minified/resource-shrunk release APK + AAB + mapping. GitHub Actions quality job’u geçtikten sonra API 34 Pixel 6 Pro üzerinde 54 Room/Compose/navigation/golden cihaz testi çalışır. Lint, release ve device raporları artifact olarak saklanır. Production signing ve fiziksel cihaz smoke’u ayrı release kapısıdır.

Definition of Done ayrıntısı: [[GELISTIRME_MASTER_PLANI#Definition of Done]].
