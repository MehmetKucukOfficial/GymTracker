---
type: feature
status: current
implementation_status: mixed
verified_commit: a8e019e
updated: 2026-07-14
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueViewModel.kt
  - app/src/main/java/com/gymtracker/ui/components/MuscleBodyMap.kt
  - app/src/main/java/com/gymtracker/domain/FatigueCalculator.kt
  - app/src/main/java/com/gymtracker/domain/MuscleGroup.kt
related_data: [WorkoutSet, WorkoutSession]
tags: [gymtracker/feature, gymtracker/feature/workload]
---

# Feature — Kas Yükü Haritası

## Mevcut yetenek

Son 7 günün setleri `reps × kg × activation` ile 31 kas grubuna dağıtılır; 38 bitmap maskede ön/arka renklenir ve tıklanır.

## Veri doğrulaması

- 82 egzersiz adı benzersiz.
- Programlardaki 108 referansın tümü kütüphanede.
- 38 maske 470×836.

## Bilinen sorunlar

- ViewModel init’te tek sorgu; yeni workout’a reaktif değil.
- Bodyweight set `weight=0` ise katkı sıfır.
- `isCompleted` dikkate alınmaz.
- Time decay, RPE/RIR ve kişisel baseline yok.
- İsim string eşleşmezse sessizce atlanır.
- “Yorgunluk %” bilimsel kesinlik izlenimi verir.
- Haritanın erişilebilir metin eşdeğeri yok.

## Hedef durum

[[ADR_005_MUSCLE_LOAD_NAMING]] uyarınca “7 Günlük Kas Yükü”. Tamamlanan setlerden reaktif Flow; bodyweight/set-score stratejisi; zaman ağırlığı; metodoloji sheet’i; legend + erişilebilir kas listesi.

## Testler

Completed/pending; weighted/bodyweight; unknown exercise; range clamp; time window; new set reactivity; mask hit/accessibility.

Bağlar: [[FEATURE_WORKOUT]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]].
