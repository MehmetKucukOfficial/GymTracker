---
type: feature
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-14
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/NutritionDao.kt
related_data: [FoodItem, FoodEntry, NutritionGoal, MealType]
tags: [gymtracker/feature, gymtracker/feature/nutrition]
---

# Feature — Beslenme

## Mevcut yetenek

Özel besin, öğün kaydı, kalori/makro toplamı ve günlük hedef. `selectedDate` StateFlow vardır ancak UI tarih değiştiremez.

## Mevcut akış

Selected day → `flatMapLatest(getEntriesForDay)` + goal Flow → `DailyNutrition`. Yeni entry gün başlangıcı timestamp’iyle yazılır.

## Bilinen sorunlar

- Gün sonu `start + 86_400_000`; DST-safe değil.
- Gerçek öğün saati kaybolur.
- Hedef sıfır/negatif olabilir; bölme NaN/Infinity.
- Progress clamp sonrası aşım rengi asla çalışmaz.
- Silent numeric defaults ve Türkçe ondalık virgül sorunu.
- Food search DAO var, UI kullanmaz.
- Home beslenme değişikliğine reaktif değil.

## Hedef durum

`LocalDate + Clock + ZoneId`, tarih pager, öğün kartlarında hızlı ekleme, recent/favorite search, locale validation, undo. FoodEntry besin değerini snapshot tutar; bu bilinçli domain kararı olarak belgelenir.

## Zorunlu testler

DST day range; goal 0 validation; aşım görünümü; decimal comma; entry change → Home update; delete undo.

Bağlar: [[FEATURE_HOME]], [[ARCH_STATE_DATA_FLOW]], [[ARCH_DATABASE]], [[DESIGN_SYSTEM_GRAPH]].
