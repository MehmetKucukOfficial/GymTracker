---
type: adr
status: accepted
implementation_status: current
verified_commit: 7917358
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/data/entities/Entities.kt
  - app/src/main/java/com/gymtracker/data/dao/NutritionDao.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
  - app/src/main/java/com/gymtracker/data/nutrition/NutritionRules.kt
  - app/src/main/java/com/gymtracker/data/measurements/MeasurementRepository.kt
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionViewModel.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsViewModel.kt
tags: [gymtracker/decision, gymtracker/data, gymtracker/feature/nutrition, gymtracker/feature/measurements]
---

# ADR 008 — Snapshot-safe edit ve undo

## Bağlam

`FoodEntry`, kullanıcı geçmişinin yazıldığı andaki besin adı ve makrolarını taşır. Bir `FoodItem` daha sonra değişirse geçmiş toplamların sessizce değişmemesi gerekir. Nutrition ve BodyMeasurement silme işlemlerinde geri alma da kimliği, tarihi ve Home/Room Flow davranışını korumalıdır.

## Karar

1. `FoodEntry` tarihsel snapshot’tır; edit güncel `FoodItem` verisini tekrar okumaz.
2. Miktar edit’i `newAmount / oldAmount` oranıyla snapshot kalori/makrolarını ölçekler, `id`, `date`, `foodItemId` ve `foodName` değerlerini korur.
3. Nutrition ve Measurement delete undo, silinen entity’nin tamamını kısa ömürlü ViewModel state’inde tutar ve aynı primary key ile yeniden insert eder.
4. `BodyMeasurement` undo için insert `REPLACE` kullanır; normal edit açık `@Update` yoludur.
5. Başarılı mutation Room Flow’unu tek kaynak yapar; Home için ayrıca elle state patch edilmez.
6. Undo UI’sı Snackbar action’dır; hata typed validation/mutation state’i olarak kalır.

## Sonuçlar

- Geçmiş beslenme toplamları FoodItem değişiminden etkilenmez.
- Undo sonrası deep link/selection/Room identity sürprizi oluşmaz.
- Process ölümü Snackbar undo penceresini kalıcılaştırmaz; bu bilinçli kısa ömürlü UI olayıdır.
- Gelecekte kalıcı çöp kutusu veya çok adımlı undo gerekirse ayrı audit/tombstone modeli gerekir.

## Kanıt

`NutritionRulesTest`, `MutationContractTest` ve `HomeDashboardRepositoryTest`; snapshot ölçekleme, aynı-ID delete/restore ve Home update reaktivitesini doğrular. Bağlar: [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ARCH_STATE_DATA_FLOW]].
