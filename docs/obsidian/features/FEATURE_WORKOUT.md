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
  - app/src/main/java/com/gymtracker/domain/MuscleGroup.kt
related_data: [WorkoutSession, WorkoutSet]
tags: [gymtracker/feature, gymtracker/feature/workout]
---

# Feature — Antrenman

## Mevcut yetenek

Session başlatır, 82 egzersiz kütüphanesinden set ekler/siler, süre ve notla bitirir, geçmiş session listeler. Liste `workouts`, detay `workouts/{sessionId}` route’undadır. Yeni session insert sonucu ID ile detaya gider; detail recreation sonrası aynı ID’den yeniden yüklenir.

## Mevcut state

- `sessions`: repository Flow → stateIn
- `activeSession/activeSets`: `SavedStateHandle` session ID → repository Room Flow combine
- Session yaratıldığı anda DB’ye yazılır.

## Kritik sorunlar

- Geçmiş session karta basınca `activeSession` olur ve “Aktif Antrenman” görünür.
- Session status/finishedAt yok; yarım kayıt ayırt edilemez.
- Activity recreation ve route ID testi geçer; tam process death ayrıca smoke edilmelidir.
- Set validation/unique order/edit yok.
- `isCompleted` UI’da kullanılmıyor.
- Exercise string ID olarak kullanılır.

## Hedef model

ACTIVE/COMPLETED/CANCELLED session; active session ve setler Room Flow; tam ekran aktif workout; auto-save; set tamamla; önceki değer; rest timer; finish summary. Geçmiş detay read-only, açık edit modu.

## Etki bağları

- [[ARCH_DATABASE]] — status, timestamps, exerciseId, position
- [[ARCH_NAVIGATION]] — ID route, full-screen shell
- [[FEATURE_MUSCLE_LOAD]] — tamamlanan setler
- [[FEATURE_PROGRAMS]] — program gününden session üretme
- [[FEATURE_HOME]] — active/summary CTA

## Zorunlu testler

Start → add → complete → process recreate → finish; abandoned resume/cancel; past detail read-only; cascade; invalid/negative set; route ID.

Tasarım: [[SCREEN_BLUEPRINT_GRAPH#Aktif workout]]. Yol haritası: [[GELISTIRME_MASTER_PLANI#Faz 4 — Workout deneyimi (8–12 gün)]].
