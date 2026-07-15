---
type: architecture
status: current
implementation_status: current
verified_commit: 41c2ceb
updated: 2026-07-16
source_files:
  - app/src/main/java/com/gymtracker/MainActivity.kt
  - app/src/main/java/com/gymtracker/ui/navigation/NavGraph.kt
  - app/src/main/java/com/gymtracker/ui/components/BottomNavigationBar.kt
tags: [gymtracker/architecture, gymtracker/navigation]
---

# Mimari — Navigasyon

## Mevcut

Compact genişlikte alt bar; 600 dp ve üzerinde navigation rail kullanılır. Her ikisi de Özet, Antrenman, Programlar, Beslenme, İlerleme olmak üzere aynı 5 hedefi, seçili hedef çözümünü ve save/restore navigasyon sözleşmesini paylaşır. `NavigationPolicy`, nested route’un sahibi olan ana hedefi ve detail ekranlarında ana navigasyon görünürlüğünü tek yerden belirler.

Ölçüm (`progress/measurements`), fotoğraf (`progress/photos`) ve kas yükü (`progress/load`) İlerleme hub’ından açılır ve alt barda İlerleme seçili kalır. `workouts/{sessionId}` ve `programs/{programId}` detail ekranları alt barı gizler; Hilt ViewModel route ID’sini `SavedStateHandle` üzerinden alır ve repository Flow’dan veriyi yeniden çözer.

## Hedef

Telefon ana hedefleri:

1. Özet
2. Antrenman
3. Programlar
4. Beslenme
5. İlerleme

Ölçüm, fotoğraf ve kas yükü İlerleme altında destination olarak kalır. Aktif workout tam ekran olduğunda ana navigasyon gizlenir. Medium/expanded shell’de navigation rail uygulanmıştır; feature bazlı list-detail düzenleri sonraki iyileştirmedir.

## Route sözleşmesi

| Route | Parametre | Veri kaynağı |
|---|---|---|
| `home` | yok | Home aggregate Flow |
| `workouts` | yok | Workout repository list Flow |
| `workouts/{sessionId}` | pozitif session ID | Workout repository detail Flow |
| `programs` | yok | Program list Flow |
| `programs/{programId}` | pozitif program ID | Program repository detail Flow |
| `nutrition` | yok | Nutrition ViewModel |
| `progress` | yok | İlerleme hub |
| `progress/measurements` | yok | Measurement Flow |
| `progress/photos` | yok | Photo repository Flow |
| `progress/load` | yok | Fatigue ViewModel |

## Değişiklik kontrol listesi

- [[ADR_002_FIVE_TARGET_NAVIGATION]] kararı
- System back/Activity recreation/selected-tab ve compact→rail Compose testleri geçiyor; harici deep link ve gerçek OS process-death bekliyor
- `SavedStateHandle`
- Bottom bar selected state
- Ana destination’larda state restore
- [[FEATURE_PROGRAMS]] satın alma dialog bug’ı
- [[FEATURE_WORKOUT]] detail argümanı
