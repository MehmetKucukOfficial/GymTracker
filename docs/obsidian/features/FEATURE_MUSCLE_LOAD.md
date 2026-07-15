---
type: feature
status: current
implementation_status: current
verified_commit: 5c3ce35
updated: 2026-07-16
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueViewModel.kt
  - app/src/main/java/com/gymtracker/ui/components/MuscleBodyMap.kt
  - app/src/main/java/com/gymtracker/domain/MuscleLoadCalculator.kt
  - app/src/main/java/com/gymtracker/data/workout/CompletedSetForLoad.kt
  - app/src/main/java/com/gymtracker/data/dao/WorkoutDao.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
  - app/src/main/java/com/gymtracker/domain/MuscleGroup.kt
related_data: [WorkoutSet, WorkoutSession]
tags: [gymtracker/feature, gymtracker/feature/workload]
---

# Feature — Kas Yükü Haritası

## Mevcut yetenek

Son yedi takvim günündeki yalnız `COMPLETED` seansların tamamlanmış setleri reaktif Room Flow ile izlenir. Bu akış en güncel geçerli `BodyMeasurement.weightKg` Flow’u ile birleştirilir. `reps × effective load × activation × recency` 31 kas grubuna dağıtılır; 38 bitmap maskede ön/arka renklenir ve tıklanır. Haritanın yanında renk bağımsız kas listesi, Düşük/Orta/Yüksek etiketleri, skor, kullanılan kalibrasyon kaynağı ve metodoloji dialogu vardır.

## Veri doğrulaması

- 82 egzersiz adı benzersiz.
- Programlardaki 108 referansın tümü kütüphanede.
- 38 maske 470×836.

## Uygulanan doğruluk sözleşmesi

- ACTIVE seans ve `isCompleted=false` setler query sınırında dışlanır.
- Sıfır kilolu set için en güncel 20–400 kg aralığındaki vücut ölçümü kullanılır; geçerli ölçüm yoksa açıkça gösterilen 20 kg güvenli varsayılanına düşülür.
- En yeni set 1.0, yedi günlük sınırdaki set 0.5 recency ağırlığı alır.
- Bilinmeyen egzersiz sayısı state/UI’da görünür; skora sessizce eklenmez.
- UI “yorgunluk yüzdesi” iddiası yapmaz; seviye ve normalize skor gösterir.
- Harita semantics açıklaması ve aynı bilgiyi veren erişilebilir liste içerir.

## Kalan kapsam

- Load query/calculator hâlâ egzersiz adıyla eşleşir; persisted `exerciseId` projeksiyona taşınmalıdır.
- RPE/RIR ve kişisel haftalık hacim baseline’ı yoktur.
- Vücut ağırlığı kalibrasyonu uygulanmıştır; hareket türüne özel bodyweight oranı ve ek ağırlığın birleşik hesabı ileride kalibre edilmelidir.
- Kalıcı screenshot golden, TalkBack cihaz turu ve daha geniş metodoloji kalibrasyonu bekler.

## Hedef durum

[[ADR_005_MUSCLE_LOAD_NAMING]] çekirdek kabulü ve measurement tabanlı bodyweight kalibrasyonu uygulandı. Sonraki hedef load projeksiyonunda stable exercise ID, hareket türüne özel bodyweight katsayısı, RPE/RIR ve kişisel haftalık baseline’dır.

## Testler

Uygulandı: completed/incomplete/active query sınırı, activation dağılımı, recency, kişisel bodyweight/fallback/invalid ölçüm, en yeni geçerli ölçüm query’si, unknown exercise, range clamp, Flow reactivity ve kalibrasyon kaynağını gösteren dark/%200 font erişilebilir UI. Kalan: gerçek midnight rollover, hareket katsayısı, TalkBack cihaz turu ve mask hit koordinat testi.

Bağlar: [[FEATURE_WORKOUT]], [[ARCH_STATE_DATA_FLOW]], [[DESIGN_SYSTEM_GRAPH]].
