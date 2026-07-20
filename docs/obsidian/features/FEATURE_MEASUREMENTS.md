---
type: feature
status: current
implementation_status: current
verified_commit: 43b523a
updated: 2026-07-20
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsViewModel.kt
  - app/src/main/java/com/gymtracker/data/measurements/MeasurementRules.kt
  - app/src/main/java/com/gymtracker/data/measurements/MeasurementRepository.kt
  - app/src/main/java/com/gymtracker/domain/LocalizedDecimal.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
  - app/src/main/res/values/strings.xml
  - app/src/main/res/values-en/strings.xml
related_data: [BodyMeasurement]
tags: [gymtracker/feature, gymtracker/feature/measurements]
---

# Feature — Vücut Ölçümleri

## Mevcut yetenek

Kilo, yağ ve sekiz çevre ölçümünü ekler; ISO özel tarih seçer, mevcut kaydı düzenler, son ölçümü siler/geri alır. Son ölçüm, önceki kayıtla kilo farkı ve tekrarsız geçmiş listesinin yanında gerçek 7/30/90/tümü kilo grafiği gösterilir. Boş durumda yönlendirici CTA vardır.

## Uygulanan doğruluk sözleşmesi

- En az bir ölçüm zorunludur.
- Kilo 20–400 kg, yağ yüzde 1–75, çevreler 10–300 cm aralığındadır.
- Virgül/nokta ondalık girdisi `LocalizedDecimal` üzerinden tek kuralla parse edilir; belirsiz binlik/ondalık biçimi reddedilir.
- Trend hesabı tarihe göre en yeni iki non-null kilo kaydını seçer; tek noktada delta üretmez.
- `MeasurementsUiState` latest/history/trend ayrımını ViewModel’de kurar; latest history’den `drop(1)` ile çıkarılır.
- `MeasurementRepository`, ekran state’ini DAO’dan ayırır. Seçilen `MeasurementChartPeriod` `SavedStateHandle` içinde korunur; indexed inclusive-start/exclusive-end query `flatMapLatest` ile grafik datasını yeniler.
- Grafik yalnız non-null gerçek kilo kayıtlarını tarih sırasıyla çizer; 0/1 noktada sahte çizgi üretmez, 2+ noktada min/max ve çoğul kayıt özetini semantics/metin olarak da sunar.
- Delete undo aynı `BodyMeasurement.id` ve tüm snapshot alanlarıyla `REPLACE` insert yapar. Karar: [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]].

## Kalan kapsam

- Not alanı ve shoulders alanı edit formunda henüz açılmamıştır; mevcut kayıt düzenlenirken korunur.
- Grafik kilo odaklıdır; yağ/çevre metrik seçici ve zoom yoktur.
- Çoklu metrik chart ve daha güçlü tarih seçici bekler; chart period gerçek OS process death sonrasında korunur.

## Hedef durum

İlerleme merkezi altında latest + delta + dönem grafiği + edit/delete undo uygulanmıştır. Sonraki hedef çoklu metrik chart, daha güçlü date picker ve expanded/list-detail deneyimidir.

## Veri/mimari etkisi

Room v8, `body_measurements(date)` index’ini explicit `MIGRATION_7_8` ile ekler. v7 fixture korunur ve index varlığı test edilir. Locale-aware parser, saf trend/period/validation kuralları ve repository sınırı aktiftir; bkz. [[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]].

## Testler

Uygulandı: boş/aralık dışı form engeli, locale decimal, en yeni iki nokta trendi, v7→v8 index migration, period query, update/delete/aynı-ID undo, Home update reaktivitesi, 0/1/2+ chart state, English resource, light/dark/%200 font + kalıcı chart/top-bar golden ve ayrı süreçte PID değişimli 90-gün checked-state restorasyonu. Takvim zone/day aralığı ortak Clock akışı ve period kurallarıyla testlidir.

Bağlar: [[FEATURE_HOME]], [[SCREEN_BLUEPRINT_GRAPH#İlerleme merkezi]], [[ARCH_DATABASE]].
