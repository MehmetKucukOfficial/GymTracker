---
type: feature
status: current
implementation_status: current
verified_commit: 341bf79
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueViewModel.kt
  - app/src/main/java/com/gymtracker/ui/components/MuscleBodyMap.kt
  - app/src/main/java/com/gymtracker/domain/MuscleLoadCalculator.kt
  - app/src/main/java/com/gymtracker/data/workout/CompletedSetForLoad.kt
  - app/src/main/java/com/gymtracker/data/dao/WorkoutDao.kt
  - app/src/main/java/com/gymtracker/domain/MuscleGroup.kt
related_data: [WorkoutSet, WorkoutSession]
tags: [gymtracker/feature, gymtracker/feature/workload]
---

# Feature — Kas Yükü Haritası

## Mevcut yetenek

Son yedi takvim günündeki yalnız `COMPLETED` seansların tamamlanmış setleri reaktif Room Flow ile izlenir. `reps × effective load × activation × recency` 31 kas grubuna dağıtılır; 38 bitmap maskede ön/arka renklenir ve tıklanır. Haritanın yanında renk bağımsız kas listesi, Düşük/Orta/Yüksek etiketleri, skor ve metodoloji dialogu vardır.

## Veri doğrulaması

- 82 egzersiz adı benzersiz.
- Programlardaki 108 referansın tümü kütüphanede.
- 38 maske 470×836.

## Uygulanan doğruluk sözleşmesi

- ACTIVE seans ve `isCompleted=false` setler query sınırında dışlanır.
- Sıfır kilolu set için açıkça belgelenmiş 20 kg bodyweight eşdeğeri kullanılır.
- En yeni set 1.0, yedi günlük sınırdaki set 0.5 recency ağırlığı alır.
- Bilinmeyen egzersiz sayısı state/UI’da görünür; skora sessizce eklenmez.
- UI “yorgunluk yüzdesi” iddiası yapmaz; seviye ve normalize skor gösterir.
- Harita semantics açıklaması ve aynı bilgiyi veren erişilebilir liste içerir.

## Kalan kapsam

- Stable exercise ID, RPE/RIR ve kişisel baseline yok.
- 20 kg bodyweight eşdeğeri geçici ürün varsayımıdır; profil kilosuna göre strateji sonraki ADR revizyonudur.
- Kalıcı screenshot golden, TalkBack cihaz turu ve daha geniş metodoloji kalibrasyonu bekler.

## Hedef durum

[[ADR_005_MUSCLE_LOAD_NAMING]] çekirdek kabulü uygulandı. Sonraki hedef stable exercise ID, profil tabanlı bodyweight stratejisi, RPE/RIR ve kişisel baseline’dır.

## Testler

Uygulandı: completed/incomplete/active query sınırı, activation dağılımı, recency, bodyweight fallback, unknown exercise, range clamp, Flow reactivity ve dark/%200 font erişilebilir UI. Kalan: gerçek midnight rollover, profile bodyweight, TalkBack cihaz turu ve mask hit koordinat testi.

Bağlar: [[FEATURE_WORKOUT]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]].
