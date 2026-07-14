---
type: feature
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/programs/ProgramsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/programs/ProgramsViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgramDao.kt
  - app/src/main/java/com/gymtracker/data/programs/ProgramRepository.kt
  - app/src/main/java/com/gymtracker/domain/ProgramExercise.kt
related_data: [Program, ProgramWorkout, ProgramDifficulty]
tags: [gymtracker/feature, gymtracker/feature/programs]
---

> Implementation update (2026-07-14): Default program seeding is now owned by `ProgramSeedInitializer`, runs at application startup, and is protected by a v4 `seed_metadata` claim inside one Room transaction. Fresh installs receive 5 programs and 23 program days exactly once; an existing v3 program list is marked handled without duplication. [[ProgramSeedInitializerTest]] passes repeated, concurrent, and legacy-data cases on API 34.

# Feature — Programlar

## Mevcut yetenek

2 ücretsiz + 3 demo premium program seed edilir. Gün detayları JSON listesi olarak saklanır. Liste `programs`, detay `programs/{programId}` route’undadır; `SavedStateHandle` ID’si repository Flow ile program ve günleri yeniden çözer. Demo purchase güncellemesi detail ekranına reaktif yansır.

## Kritik sorunlar

- Default seed artık ViewModel'de değil; application başlangıcında transaction ile çalışır.
- `seed_metadata` atomik claim ile duplicate ve yarım yazımı engeller.
- Yeni içerik sürümü için yeni seed key/content version ve test gerekir.
- Detail route ve Activity recreation UI testi geçer; harici deep link tanımı bekliyor.
- Bozuk JSON composable parse sırasında crash üretir.
- `Float` fiyat ve local boolean gerçek entitlement olamaz.

## Hedef durum

Seed repository/DB callback içinde versioned + idempotent transaction. ProgramDayExercise ilişkisel tablo. ID tabanlı detail Flow. Programdan aktif workout üretme. Demo entitlement açık; gerçek billing ayrı roadmap.

## Etki bağları

- [[ARCH_DATABASE]] — normalizasyon/migration
- [[ARCH_NAVIGATION]] — detail route
- [[FEATURE_WORKOUT]] — programa başla
- [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]]

## Testler

Concurrent seed; rollback; JSON→table migration; detail deep link/back; purchase dialog/state; program→workout copy.
