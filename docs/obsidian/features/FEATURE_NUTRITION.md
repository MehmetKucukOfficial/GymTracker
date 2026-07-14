---
type: feature
status: current
implementation_status: current
verified_commit: f9d6d75
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionViewModel.kt
  - app/src/main/java/com/gymtracker/data/nutrition/NutritionRules.kt
  - app/src/main/java/com/gymtracker/domain/LocalizedDecimal.kt
  - app/src/main/java/com/gymtracker/di/TimeModule.kt
  - app/src/main/java/com/gymtracker/data/dao/NutritionDao.kt
related_data: [FoodItem, FoodEntry, NutritionGoal, MealType]
tags: [gymtracker/feature, gymtracker/feature/nutrition]
---

# Feature — Beslenme

## Mevcut yetenek

Özel besin, öğün kaydı, kalori/makro toplamı ve günlük hedef. UI önceki gün, sonraki gün ve bugün arasında dolaşır; gelecek gün engellenir. Kalori aşımı, güvenli makro ilerlemesi, boş durum ve işlem hataları açık state olarak gösterilir.

## Mevcut akış

`SavedStateHandle` içindeki ISO `LocalDate` → `nutritionDayRange(date, Clock.zone)` → `flatMapLatest(getEntriesForDay)` + goal Flow → `DailyNutrition`. Takvim günü başlangıç/bitişi DST-safe hesaplanır. Bugünkü entry `Clock.instant()`, geçmiş gün entry’si seçilen günün yerel saatiyle yazılır.

Ondalık girdiler ortak `LocalizedDecimal` ile virgül/nokta destekli parse edilir. Besin miktarı, besin değerleri ve pozitif hedefler yazmadan önce doğrulanır; başarısız Room işlemi dialogu kapatmaz.

## Kalan kapsam

- Food search DAO var, UI kullanmaz.
- Entry silme için undo yok.
- FoodEntry besin değerini snapshot olarak tutar; ürün güncellemesi geçmiş kayıtları değiştirmez. Bu domain sözleşmesi ileride ADR ile sabitlenmelidir.
- Seçili gün process recreation’da korunur; uygulama açıkken gece yarısı seçimi otomatik ilerlemez.
- İkinci locale ve gerçek process-death testi bekliyor.

## Hedef durum

Recent/favorite food search, öğün kartında hızlı ekleme, silme undo’su, snapshot ADR’si ve ikinci locale kapsamı. Home beslenme Flow’una zaten reaktiftir.

## Zorunlu testler

Uygulandı: 23/25 saat DST day range, goal 0 güvenliği, aşım görünümü, decimal comma ve dark/%200 font Compose kabulü. Kalan: entry change → Home entegrasyon testi, delete undo, ikinci locale ve gerçek process-death.

Bağlar: [[FEATURE_HOME]], [[ARCH_STATE_DATA_FLOW]], [[ARCH_DATABASE]], [[DESIGN_SYSTEM_GRAPH]].
