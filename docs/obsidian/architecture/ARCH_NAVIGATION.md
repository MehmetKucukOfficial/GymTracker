---
type: architecture
status: current
implementation_status: mixed
verified_commit: a8e019e
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/MainActivity.kt
  - app/src/main/java/com/gymtracker/ui/navigation/NavGraph.kt
  - app/src/main/java/com/gymtracker/ui/components/BottomNavigationBar.kt
tags: [gymtracker/architecture, gymtracker/navigation]
---

# Mimari — Navigasyon

## Mevcut

Telefon alt barı: Özet, Antrenman, Programlar, Beslenme, İlerleme — toplam 5 öğe. `NavigationPolicy`, nested route’un sahibi olan ana hedefi ve detail ekranlarında alt bar görünürlüğünü tek yerden belirler.

Ölçüm (`progress/measurements`), fotoğraf (`progress/photos`) ve kas yükü (`progress/load`) İlerleme hub’ından açılır ve alt barda İlerleme seçili kalır. `workouts/{sessionId}` ve `programs/{programId}` detail ekranları alt barı gizler; Hilt ViewModel route ID’sini `SavedStateHandle` üzerinden alır ve repository Flow’dan veriyi yeniden çözer.

## Hedef

Telefon ana hedefleri:

1. Özet
2. Antrenman
3. Programlar
4. Beslenme
5. İlerleme

Ölçüm, fotoğraf ve kas yükü İlerleme altında destination olarak kalır. Aktif workout tam ekran olduğunda alt bar gizlenir. Tablet medium/expanded düzende navigation rail ve list-detail kullanılır.

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
- System back/process recreation/selected-tab Compose testleri geçiyor; harici deep link bekliyor
- `SavedStateHandle`
- Bottom bar selected state
- Ana destination’larda state restore
- [[FEATURE_PROGRAMS]] satın alma dialog bug’ı
- [[FEATURE_WORKOUT]] detail argümanı
