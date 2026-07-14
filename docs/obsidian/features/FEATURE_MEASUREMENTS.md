---
type: feature
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-14
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
related_data: [BodyMeasurement]
tags: [gymtracker/feature, gymtracker/feature/measurements]
---

# Feature — Vücut Ölçümleri

## Mevcut yetenek

Kilo, yağ ve sekiz çevre ölçümünü ekler; son ölçüm kartı ve geçmiş listesi gösterir; siler.

## Bilinen sorunlar

- Tamamen boş/all-null kayıt kabul edilir.
- Negatif/gerçek dışı değer ve decimal locale doğrulaması yok.
- Son kayıt hem summary hem history’de tekrar görünür.
- Edit, tarih seçimi, undo ve grafik yok.
- Home yeni ölçüme reaktif değil.

## Hedef durum

İlerleme merkezi altında latest + delta + 7/30/90/all trend; form validation; tarih seçimi; edit/delete/undo. Tek veri noktası varsa trend oku yok.

## Veri/mimari etkisi

`body_measurements(date)` index; repository/domain model; locale-aware value parser; chart UI. Entity değişirse [[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]].

## Testler

Boş/negatif form engeli; locale decimal; update/delete; date order; trend için 0/1/2+ nokta; Home reactivity.

Bağlar: [[FEATURE_HOME]], [[SCREEN_BLUEPRINT_GRAPH#İlerleme merkezi]], [[ARCH_DATABASE]].
