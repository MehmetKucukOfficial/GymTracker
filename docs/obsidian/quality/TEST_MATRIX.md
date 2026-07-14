---
type: quality
status: current
implementation_status: mixed
verified_commit: 4d0cad8
updated: 2026-07-15
tags: [gymtracker/quality, gymtracker/testing]
---

# Test Matrisi

## Mevcut temel (2026-07-15)

- JVM unit test altyapisi `JUnit 4` ile aktiftir; `testDebugUnitTest` artik `NO-SOURCE` degildir.
- [[FatigueCalculatorTest]] bilinen egzersiz aktivasyon dagilimini, cap davranisini ve bilinmeyen egzersiz davranisini korur.
- Room instrumentation test altyapisi AndroidJUnitRunner ve `room-testing` ile aktiftir.
- [[WorkoutDaoTest]] tarih sinirli set sorgusunu ve session -> set cascade davranisini cihaz/emulator calistirmasi icin tanimlar.
- [[DatabaseMigrationTest]] v3→v6 migration’ında workout ve legacy fotoğraf satırlarının korunmasını, fotoğraf metadata varsayılanlarını ve legacy workout’ın güvenli `COMPLETED/completedAt` dönüşümünü doğrular.
- [[ProgramSeedInitializerTest]] tekrarlı, eşzamanlı ve legacy seed durumlarını kapsar.
- [[ProgressPhotoRepositoryTest]] app-owned dosya kopyası, metadata, storage yeniden oluşturma, delete cleanup, kaynak/DB hata rollback ve 25 MB sınırında geçici dosya temizliğini kapsar.
- [[NavigationContractTest]] 5 hedef sırasını, nested route sahipliğini, detail görünürlük politikasını ve pozitif ID kuralını doğrular.
- [[HomeDashboardPeriodTest]] Istanbul takvim/DST gün sınırını; [[HomeDashboardRepositoryTest]] completed/active workout, measurement ve nutrition Flow’larının reaktif birleşimini doğrular.
- [[NavigationSmokeTest]] back, selected tab, workout/program ID detail ve Activity recreation davranışını gerçek Compose UI üzerinde doğrular.
- `HomeLayoutConfigTest` compact/expanded ve %200 font kolon politikasını JVM’de doğrular.
- `HomeScreenTest` light/dark render, %200 font empty state, sahte sıfır yokluğu, birleşik metric semantics ve error retry davranışını doğrular.
- `WorkoutRepositoryTest` eşzamanlı start/resume çağrılarının tek aktif ID üretmesini ve tamamlanan seansın mutation’lara kapanmasını doğrular.
- `WorkoutScreenTest` aktif/geçmiş ayrımını, tamamlanan detayın salt okunurluğunu ve light/dark + %200 font yerleşimini doğrular.
- Pixel 6 Pro API 34 emülatöründe 23 instrumentation testi ve 11 JVM testi, 0 failure ile geçer.

## Feature matrisi

| Alan | Unit | Room/instrumentation | Compose/UI | Smoke |
|---|---|---|---|---|
| Home | aggregate + clock + adaptive policy | summary queries | empty/content/error, semantics, light/dark, %200 font | dashboard açılış |
| Workout | state/validation | v3→v6, transaction/cascade/single-active/read-only | active/history, light/dark, %200 font | Activity recreation; process death bekliyor |
| Nutrition | totals/date/parser | day query/goal | add/delete/undo | DST day |
| Measurements | validation/trend | order/update | form/chart | Home update |
| Photos | metadata/limit policy | migration/import/delete rollback | picker/progress/error/viewer | process kill/restart/reboot |
| Programs | seed/content | idempotency/migration | detail/purchase | program→workout |
| Muscle load | formula/time decay | recent sets query | map/list/accessibility | new set update |

## Room ve migration

- v3→latest (v6) doğrulandı; v1/v2 tarihi şema kanıtı olmadığı için destek iddiası yok
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

Home’un takvim sınırı saf fonksiyonla testlidir; canlı rollover akışına Clock/ZoneId injection henüz eklenmedi.

## CI sırası

Compile → unit → lint/format/detekt → Room/migration → Compose/screenshot → assemble. Release branch ayrıca instrumentation, R8 bundle ve smoke.

Definition of Done ayrıntısı: [[GELISTIRME_MASTER_PLANI#Definition of Done]].
