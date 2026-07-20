---
type: feature
status: current
implementation_status: current
verified_commit: 43b523a
updated: 2026-07-20
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/home/HomeScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/home/HomeViewModel.kt
  - app/src/main/java/com/gymtracker/data/home/HomeDashboardRepository.kt
  - app/src/main/java/com/gymtracker/data/home/HomeDashboardPeriod.kt
  - app/src/main/java/com/gymtracker/ui/components/GymTrackerComponents.kt
  - app/src/main/java/com/gymtracker/ui/theme/DesignTokens.kt
related_data: [WorkoutSession, BodyMeasurement, FoodEntry]
tags: [gymtracker/feature, gymtracker/feature/home]
---

# Feature — Home / Özet

## Amaç ve mevcut çıktı

Route `home`, başlangıç destination’dır. Ana antrenman CTA’sı aktif seans yoksa yeni akışa, varsa doğrudan o session ID’sine gider. Açıklayıcı empty state; yalnızca tamamlanan kayıtlardan toplam/haftalık workout ve son workout, ayrıca son kilo, bugünkü kalori ve beş hızlı işlem sunar. Kayıt yokken sahte `0` yerine “Henüz yok/Ölçüm yok/Öğün yok” gösterir.

## Mevcut akış

Workout completed/active, measurement ve günün nutrition Room Flow’ları → `HomeDashboardRepository.combine` → `HomeViewModel` `Loading/Content/Error` → lifecycle-aware `HomeScreen`. Her veri kaynağındaki değişim aynı açık ekranda dashboard’u yeniler. Home ve Kas Yükü aynı injectable `Clock` tabanlı `currentDashboardPeriods` akışını kullanır. Akış yerel gece yarısında yeni aralığı yayınlar; dinamik sistem `ZoneId` değişimini en geç 60 saniyede yeniden hesaplar, aynı aralığı gereksiz yere tekrar yayınlamaz. Retry aynı akışı yeniden başlatır.

## Bağımlılıklar

- [[FEATURE_WORKOUT]]
- [[FEATURE_NUTRITION]]
- [[FEATURE_MEASUREMENTS]]
- [[ARCH_NAVIGATION]]
- [[ARCH_STATE_DATA_FLOW]]

## Kalan sınırlar

- Compact/expanded içerik kolonları var; Home için kalıcı liste–detay gerekmiyor.
- Home’a özel kalıcı golden yok; uygulama genelinde altı kritik golden aktiftir.

## Hedef durum

Reaktif özet, tasarım sistemi, [[FEATURE_WORKOUT]] aktif-seans CTA entegrasyonu ve Clock/ZoneId dayanıklılığı tamamlandı. Sonraki hedef, gerçek trend datası geldiğinde mini grafiği eklemektir.

## Değişiklik dosya kümesi

İlk hedef dosyalar: Home screen/VM, yeni repository/use case, DAO Flow sorguları, navigation shell, string/theme kaynakları. Bütün depoyu değil [[SOURCE_FILE_ATLAS#Home]] bölümünü doğrula.

## Zorunlu testler

- Completed workout/active workout/measurement/nutrition ayrı ayrı değişince state yenilenir; aktif kayıt toplam ve son-workout metriklerini şişirmez: API 34 repository testi geçiyor.
- Tarihsel Istanbul DST sınırı, enjekte Clock ile gerçek yerel gece yarısı geçişi ve çalışma anındaki UTC→Tokyo zone değişimi JVM testinde deterministik olarak doğrulandı.
- Empty/content/error ve retry Compose testleri geçiyor; loading state açıkça modelleniyor.
- Light/dark, %200 font ve compact davranışı API 34 test/görselleriyle doğrulandı; kolon politikası JVM testlidir.

Roadmap: [[ROADMAP_EXECUTION_GRAPH#PR 7 — Design system + Home]].
