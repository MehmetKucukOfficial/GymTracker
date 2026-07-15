---
type: quality
status: current
implementation_status: mixed
verified_commit: 2ad7ad8
updated: 2026-07-15
tags: [gymtracker/quality, gymtracker/testing]
---

# Test Matrisi

## Mevcut temel (2026-07-15)

- JVM unit test altyapisi `JUnit 4` ile aktiftir; `testDebugUnitTest` artik `NO-SOURCE` degildir.
- [[FatigueCalculatorTest]] bilinen egzersiz aktivasyon dagilimini, cap davranisini ve bilinmeyen egzersiz davranisini korur.
- Room instrumentation test altyapisi AndroidJUnitRunner ve `room-testing` ile aktiftir.
- [[WorkoutDaoTest]] tarih sinirli set sorgusunu ve session -> set cascade davranisini cihaz/emulator calistirmasi icin tanimlar.
- [[DatabaseMigrationTest]] v3→v7 satır korumasını; legacy workout/photo varsayılanlarını; v6 program JSON→ilişkisel satır dönüşümünü; bozuk JSON fallback’ini ve yeni WorkoutSet kolonlarını doğrular.
- [[ProgramSeedInitializerTest]] tekrarlı, eşzamanlı ve legacy seed durumlarını kapsar.
- [[ProgressPhotoRepositoryTest]] app-owned dosya kopyası, metadata, storage yeniden oluşturma, delete cleanup, kaynak/DB hata rollback ve 25 MB sınırında geçici dosya temizliğini kapsar.
- [[NavigationContractTest]] 5 hedef sırasını, nested route sahipliğini, detail görünürlük politikasını ve pozitif ID kuralını doğrular.
- [[HomeDashboardPeriodTest]] Istanbul takvim/DST gün sınırını; [[HomeDashboardRepositoryTest]] completed/active workout, measurement ve nutrition Flow’larının reaktif birleşimini doğrular.
- [[NavigationSmokeTest]] back, selected tab, workout/program ID detail ve Activity recreation davranışını gerçek Compose UI üzerinde doğrular.
- `HomeLayoutConfigTest` compact/expanded ve %200 font kolon politikasını JVM’de doğrular.
- `HomeScreenTest` light/dark render, %200 font empty state, sahte sıfır yokluğu, birleşik metric semantics ve error retry davranışını doğrular.
- `WorkoutRepositoryTest` eşzamanlı start/resume çağrılarının tek aktif ID üretmesini ve tamamlanan seansın mutation’lara kapanmasını doğrular.
- `WorkoutScreenTest` aktif/geçmiş ayrımını, tamamlanan detayın salt okunurluğunu ve light/dark + %200 font yerleşimini doğrular.
- `LocalizedDecimalTest`, `NutritionRulesTest` ve `MeasurementRulesTest` virgül/nokta parse, DST 23/25 saat, zero/over-goal, input aralıkları ve en yeni iki kilo trendini doğrular.
- `NutritionScreenTest` over-goal/empty state’i; `MeasurementsScreenTest` latest/history ayrımını ve empty state’i dark + %200 font koşulunda doğrular.
- `ProgressPhotoRulesTest`, `ProgressPhotoRepositoryTest` ve `ProgressPhotoScreenTest` fotoğraf input aralığı, import/rollback/limit, orphan/missing reconciliation, legacy badge ve %200 font empty state’ini doğrular.
- `MuscleLoadCalculatorTest`, `WorkoutDaoTest` ve `MuscleLoadScreenTest` recency/bodyweight/unknown/cap kurallarını, completed-only reaktif query’yi ve renk bağımsız adaptive UI’ı doğrular.
- `ExerciseIdentityTest`, `ProgramSeedInitializerTest`, `ProgramRepositoryTest`, `WorkoutRepositoryTest` ve `ProgramsScreenTest`; unique/locale-independent ID, 5/23/108 seed, locked guard, atomic plan copy, incomplete-set finish engeli ve adaptive UI’yı doğrular.
- Pixel 6 Pro API 34 emülatöründe 41 instrumentation testi ve 22 JVM testi, 0 failure ile geçer.

## Feature matrisi

| Alan | Unit | Room/instrumentation | Compose/UI | Smoke |
|---|---|---|---|---|
| Home | aggregate + clock + adaptive policy | summary queries | empty/content/error, semantics, light/dark, %200 font | dashboard açılış |
| Workout | state/validation | v3→v7, transaction/cascade/single-active/read-only/planned set | active/history/planned completion, light/dark, %200 font | Activity recreation; process death bekliyor |
| Nutrition | totals/DST/parser/goal | day query/goal | over-goal/empty/dark/%200 | delete undo + Home update bekliyor |
| Measurements | validation/trend | order/update bekliyor | latest/history/empty/dark/%200 | Home update bekliyor |
| Photos | input/limit/backup policy | migration/import/delete/reconcile rollback | empty/legacy/viewer/dark/%200 | process kill/restart/reboot bekliyor |
| Programs | stable ID/content | idempotent 5/23/108 seed, v6→v7 JSON migration, atomic plan copy | list/detail/locked/start, dark/%200 | program→workout + Activity recreation |
| Muscle load | activation/decay/bodyweight/unknown | completed-only set Flow | map/list/methodology/dark/%200 | midnight + TalkBack bekliyor |

## Room ve migration

- v3→latest (v7) doğrulandı; v1/v2 tarihi şema kanıtı olmadığı için destek iddiası yok
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

Compile → unit → lint/format/detekt → Room/migration → Compose/screenshot → assemble. Release branch ayrıca instrumentation, R8 bundle ve smoke.

Definition of Done ayrıntısı: [[GELISTIRME_MASTER_PLANI#Definition of Done]].
