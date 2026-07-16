---
type: design
status: current
implementation_status: current
verified_commit: 341bf79
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/theme/Theme.kt
  - app/src/main/java/com/gymtracker/ui/theme/DesignTokens.kt
  - app/src/main/java/com/gymtracker/ui/components/GymTrackerComponents.kt
  - app/src/main/java/com/gymtracker/ui/components/BottomNavigationBar.kt
  - app/src/main/java/com/gymtracker/ui/screens/workout/WorkoutScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsScreen.kt
  - app/src/main/res/values/themes.xml
  - app/src/main/res/values-night/themes.xml
tags: [gymtracker/design, gymtracker/architecture]
---

# Tasarım Sistemi Grafı

## Uygulanan temel

- `GymTrackerTheme`, sistem moduna bağlı light/dark Material 3 renk şeması kullanır; platform açılış penceresi de `values`/`values-night` ile aynı yüzeye geçer.
- `DesignTokens.kt`: ortak typography, spacing, shape ve minimum 48 dp touch/icon/content-width ölçülerini tanımlar.
- `GymTrackerComponents.kt`: `GtSectionHeader`, `GtMetricCard`, `GtQuickActionCard`, `GtLoadingState`, `GtEmptyState`, `GtErrorState` bileşenlerini sağlar.
- Home referans ekran olarak bu bileşenlere taşındı; bottom navigation %200 fontta yalnız seçili etiketi göstererek çok satırlı taşmayı önler.
- Workout aktif/geçmiş liste ve detayları aynı token/state bileşenlerini tüketir; %200 fontta kart ve detay başlıkları dikey yerleşime geçer.
- Nutrition tarih/kalori/makro/öğün ve Measurements latest/trend/history/empty yüzeyleri ortak tokenları tüketir; %200 fontta yatay metrikler dikeyleşir.
- Kullanıcıya görünen Home, Workout, Nutrition, Measurements ve alt navigasyon metinleri string resource’a taşındı.

## Token ve tüketim grafı

```mermaid
flowchart LR
    Theme --> Colors
    Theme --> Type[Typography]
    Theme --> Shape[Radius/Shape]
    Theme --> Space[Spacing]
    Theme -. bekliyor .-> Motion
    Colors --> Components
    Type --> Components
    Shape --> Components
    Space --> Components
    Components --> Screens
```

Detaylı değerler: [[GELISTIRME_MASTER_PLANI#5. Görsel tasarım yönü]].

## Ortak bileşen durumu

- `current`: GtMetricCard, GtSectionHeader, GtQuickActionCard, GtEmptyState, GtLoadingState, GtErrorState
- `partial`: adaptive Home/Workout/Nutrition/Measurements content ve font-aware bottom navigation
- `next`: GtAdaptiveScaffold / GtTopAppBar
- `next`: GtPrimaryButton / GtTonalButton / GtProgressCard / skeleton
- GtNumberField / GtNumberStepper
- GtDateHeader
- GtExerciseRow / GtSetRow
- GtChartCard / GtSnackbarHost

## Kalite sözleşmesi ve kanıt

Her bileşen: normal/disabled/loading/error, light/dark, büyük font preview, 48dp touch, semantics, screenshot testi. Primary renk error yerine kullanılmaz; renk tek bilgi kanalı değildir. PR7 Home, PR8 Workout ve PR9 Nutrition/Measurements light/dark/%200 font gerçek API 34 görselleriyle; state’ler Compose UI testleriyle doğrulandı. Disabled/loading varyantları ve kalıcı golden altyapısı bütün bileşenler için henüz tamamlanmadı.

Görsel kanıtlar (build artifact): PR7 Home raporları; PR8 Workout görselleri; `app/build/pr9-nutrition-light-final.png`, `pr9-nutrition-dark-font200-final.png`, `pr9-measurements-dark-font200-final.png`.

## Uygulama sırası

Home, active/read-only Workout ve Nutrition/Measurements çekirdek ekranları tamamlandı → photos/load → programs → uygulama geneli adaptive/golden. Ekran hedefleri: [[SCREEN_BLUEPRINT_GRAPH]].
