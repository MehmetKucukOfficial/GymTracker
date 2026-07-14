---
type: feature
status: current
implementation_status: current
verified_commit: 4d0cad8
updated: 2026-07-15
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

Workout completed/active, measurement ve günün nutrition Room Flow’ları → `HomeDashboardRepository.combine` → `HomeViewModel` `Loading/Content/Error` → lifecycle-aware `HomeScreen`. Her veri kaynağındaki değişim aynı açık ekranda dashboard’u yeniler. ViewModel, sonraki yerel gün başlangıcına kadar bekleyip `flatMapLatest` ile sorgu aralığını yeniden kurar; retry aynı akışı yeniden başlatır.

## Bağımlılıklar

- [[FEATURE_WORKOUT]]
- [[FEATURE_NUTRITION]]
- [[FEATURE_MEASUREMENTS]]
- [[ARCH_NAVIGATION]]
- [[ARCH_STATE_DATA_FLOW]]

## Kalan sınırlar

- Gece yarısı ticker’ı uygulanmış olsa da Clock/ZoneId injection ve zone-change testi bekliyor.
- Compact/expanded içerik kolonları var; uygulama geneli navigation rail ve list-detail henüz yok.
- Kalıcı screenshot golden/diff altyapısı yok; PR7 kanıtları build artifact olarak tutuluyor.

## Hedef durum

Reaktif özet, tasarım sistemi ve [[FEATURE_WORKOUT]] aktif-seans CTA entegrasyonu tamamlandı. Sonraki hedef, gerçek trend datası geldiğinde mini grafiği eklemek ve Clock/ZoneId bağımlılıklarını test için enjekte etmektir.

## Değişiklik dosya kümesi

İlk hedef dosyalar: Home screen/VM, yeni repository/use case, DAO Flow sorguları, navigation shell, string/theme kaynakları. Bütün depoyu değil [[SOURCE_FILE_ATLAS#Home]] bölümünü doğrula.

## Zorunlu testler

- Completed workout/active workout/measurement/nutrition ayrı ayrı değişince state yenilenir; aktif kayıt toplam ve son-workout metriklerini şişirmez: API 34 repository testi geçiyor.
- Tarihsel Istanbul DST sınırı unit testte doğrulandı; ViewModel otomatik rollover yapıyor, enjekte Clock ile deterministik ticker testi bekliyor.
- Empty/content/error ve retry Compose testleri geçiyor; loading state açıkça modelleniyor.
- Light/dark, %200 font ve compact davranışı API 34 test/görselleriyle doğrulandı; kolon politikası JVM testlidir.

Roadmap: [[ROADMAP_EXECUTION_GRAPH#PR 7 — Design system + Home]].
