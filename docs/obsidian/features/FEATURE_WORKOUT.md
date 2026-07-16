---
type: feature
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/workout/WorkoutScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/workout/WorkoutViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/WorkoutDao.kt
  - app/src/main/java/com/gymtracker/data/workout/WorkoutRepository.kt
  - app/src/main/java/com/gymtracker/data/entities/Entities.kt
  - app/src/main/java/com/gymtracker/data/database/DatabaseMigrations.kt
  - app/src/main/java/com/gymtracker/domain/MuscleGroup.kt
related_data: [WorkoutSession, WorkoutSet]
tags: [gymtracker/feature, gymtracker/feature/workout]
---

# Feature — Antrenman

## Mevcut yetenek

Tek bir `ACTIVE` session başlatır veya devam ettirir; 82 egzersiz kütüphanesinden doğrulanmış set ekler/siler, süre ve notla bitirip `COMPLETED` yapar ve geçmişi salt okunur gösterir. Liste `workouts`, detay `workouts/{sessionId}` route’undadır. Home ve Workout CTA’ları aktif ID varsa aynı detaya devam eder; detail recreation sonrası aynı ID’den yeniden yüklenir.

## Mevcut state

- `activeSession`: DAO status Flow → repository → ViewModel `stateIn`.
- `completedSessions`: yalnızca `COMPLETED` satırlar → tarih sıralı geçmiş.
- Detail `activeSession/activeSets`: `SavedStateHandle` session ID → repository Room Flow combine; değişken adı route detayını ifade eder, status’u değil.
- Start/resume, set mutation ve finish status kuralları Room transaction/koşullu SQL ile korunur.
- Hatalı set ve salt-okunur mutation girişimleri açık `WorkoutMutationError` durumuna çevrilir.

## Kritik sorunlar

- Activity recreation ve route ID testi geçer; tam process death ayrıca smoke edilmelidir.
- DB, status alanında tek-active unique constraint taşımıyor; garanti uygulamanın tek transaction yazma yoluna dayanır. Çok süreçli/yabancı DB writer desteği eklenirse partial unique index gerekir.
- Set validation var; unique sıra/edit/undo yok.
- `isCompleted` UI’da kullanılmıyor.
- Exercise string ID olarak kullanılır.

## Hedef model

`ACTIVE/COMPLETED`, tek aktif seans, auto-save ve read-only geçmiş tamamlandı. Sonraki hedefler: açık `CANCELLED` davranışı, set edit/undo ve sıralama, önceki değer, rest timer, finish summary ve isteğe bağlı açık geçmiş-düzenleme modu.

## Etki bağları

- [[ARCH_DATABASE]] — status, timestamps, exerciseId, position
- [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] — durum ve legacy migration kararı
- [[ARCH_NAVIGATION]] — ID route, full-screen shell
- [[FEATURE_MUSCLE_LOAD]] — tamamlanan setler
- [[FEATURE_PROGRAMS]] — program gününden session üretme
- [[FEATURE_HOME]] — active/summary CTA

## Zorunlu testler

- Geçen: eşzamanlı start/resume → tek ID; add/delete/finish yalnızca ACTIVE; finish sonrası salt-okunur; legacy v3→v6; Home completed/active ayrımı.
- Geçen: Compose aktif/geçmiş ayrımı, salt-okunur mutation yokluğu, light/dark ve %200 font; API 34 activity recreation/route ID.
- Bekleyen: gerçek OS process death, cancel/abandon UX, set edit/undo, golden screenshot diff.

Tasarım: [[SCREEN_BLUEPRINT_GRAPH#Aktif workout]]. Yol haritası: [[GELISTIRME_MASTER_PLANI#Faz 4 — Workout deneyimi (8–12 gün)]].
