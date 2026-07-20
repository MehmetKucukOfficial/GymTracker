---
type: quality
status: current
implementation_status: measured-emulator
verified_commit: e1359bb
updated: 2026-07-20
source_files:
  - baselineprofile/build.gradle.kts
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/BaselineProfileGenerator.kt
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/CriticalUserJourneys.kt
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/StartupBenchmark.kt
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/JourneyBenchmark.kt
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/ProcessDeathNavigationTest.kt
  - app/src/release/generated/baselineProfiles/baseline-prof.txt
  - app/src/release/generated/baselineProfiles/startup-prof.txt
tags: [gymtracker/quality, gymtracker/performance, gymtracker/release]
---

# Performance Baseline

## Ölçüm sözleşmesi

- Startup Profile: cold launch → first display.
- Genel Baseline Profile: Home açılışı + iki yönlü scroll + Workout top-level navigation.
- Macrobenchmark: cold startup `CompilationMode.None` ve `Partial`; Home scroll ve Workout açılışı `FrameTimingMetric`.
- Beş tekrar ve Perfetto trace üretilir. CUJ helper’ları profil üretici ve benchmark tarafından ortak kullanılır.
- Aynı ayrı-process test modülü, performans metriğinden bağımsız `ProcessDeathNavigationTest` ile hedef uygulamanın PID değişimi + Android task restorasyonunu kabul kapısına bağlar; bu iki test macrobenchmark ölçümü değildir.
- PR20 merge commit `625489a` otomatik master koşusu `29762392465` içinde iki test hosted API 34 üzerinde geçti; indirilen artifact XML 2/2, 0 failure/error/skipped sonucunu doğrular. PR21 `e1359bb` sonrasında aynı paket yerelde yeniden 2/2 geçti.

## İlk API 34 emülatör baseline’ı

| Senaryo | Sinyal |
|---|---|
| Cold startup, None | median 1303,6 ms; min 1163,3; max 1683,5 |
| Cold startup, Baseline Profile | median 1289,1 ms; min 1129,6; max 1484,8 |
| Home scroll | frame CPU P50 168,4 ms; P90 188,3 ms; emulator jank yüksek |
| Workout açılışı | frame CPU P50 120,8 ms; P90 186,7 ms; ilk iterasyon dışında frame örneği yok |

Bu değerler emulator/userdebug ve yaklaşık 2 GB RAM koşulundadır; ürün performans hedefi olarak kullanılamaz. Yalnız senaryoların çalıştığını ve karşılaştırma formatını kanıtlar.

## Kabul kapısı

1. Aynı release commit’i orta sınıf fiziksel API 34 cihazda, şarj/termal durum sabitken ölçülür.
2. None vs Partial startup median karşılaştırılır; en az üç bağımsız seri saklanır.
3. Home/Workout trace’lerinde jank kök nedenleri sınıflandırılır; iyileştirme yalnız ölçümle kabul edilir.
4. Release APK/AAB compiled `baseline.prof` içermelidir; `ciQuality` bunu otomatik doğrular.

Bağlar: [[ARCH_BUILD_DI]], [[TEST_MATRIX]], [[FEATURE_HOME]], [[FEATURE_WORKOUT]], [[ADR_009_SECRET_BACKED_RELEASES]].
