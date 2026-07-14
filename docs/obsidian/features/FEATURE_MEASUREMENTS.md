---
type: feature
status: current
implementation_status: current
verified_commit: 341bf79
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsViewModel.kt
  - app/src/main/java/com/gymtracker/data/measurements/MeasurementRules.kt
  - app/src/main/java/com/gymtracker/domain/LocalizedDecimal.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
related_data: [BodyMeasurement]
tags: [gymtracker/feature, gymtracker/feature/measurements]
---

# Feature — Vücut Ölçümleri

## Mevcut yetenek

Kilo, yağ ve sekiz çevre ölçümünü ekler; son ölçüm, önceki kayıtla kilo farkı ve son kaydı tekrar etmeyen geçmiş listesini gösterir; siler. Boş durumda yönlendirici CTA vardır.

## Uygulanan doğruluk sözleşmesi

- En az bir ölçüm zorunludur.
- Kilo 20–400 kg, yağ yüzde 1–75, çevreler 10–300 cm aralığındadır.
- Virgül/nokta ondalık girdisi `LocalizedDecimal` üzerinden tek kuralla parse edilir; belirsiz binlik/ondalık biçimi reddedilir.
- Trend hesabı tarihe göre en yeni iki non-null kilo kaydını seçer; tek noktada delta üretmez.
- `MeasurementsUiState` latest/history/trend ayrımını ViewModel’de kurar; latest history’den `drop(1)` ile çıkarılır.

## Kalan kapsam

- Edit, özel tarih seçimi, delete undo ve 7/30/90/all grafik yok.
- `body_measurements(date)` index’i ve repository/use-case sınırı sonraki chart diliminde değerlendirilecek; bu PR Room şemasını değiştirmez.
- Home yeni ölçüm Flow’una reaktiftir; ekranlar arası entegrasyon testi genişletilmelidir.

## Hedef durum

İlerleme merkezi altında latest + delta uygulandı. Sonraki hedef 7/30/90/all grafik, tarih seçimi, edit/delete undo ve ikinci locale testidir.

## Veri/mimari etkisi

Locale-aware parser ve saf trend/validation domain kuralları uygulandı. Chart sorgusu için `body_measurements(date)` index’i gerekirse explicit Room migration ile eklenir; bkz. [[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]].

## Testler

Uygulandı: boş/aralık dışı form engeli, locale decimal, en yeni iki nokta trendi, latest/history ayrımı ve dark/%200 font Compose kabulü. Kalan: update/delete/undo, tarih sırası DAO testi, 0/1/2+ grafik, Home reactivity entegrasyonu ve ikinci locale.

Bağlar: [[FEATURE_HOME]], [[SCREEN_BLUEPRINT_GRAPH#İlerleme merkezi]], [[ARCH_DATABASE]].
