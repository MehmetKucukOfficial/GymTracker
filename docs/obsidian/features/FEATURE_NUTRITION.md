---
type: feature
status: current
implementation_status: current
verified_commit: 341bf79
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/nutrition/NutritionViewModel.kt
  - app/src/main/java/com/gymtracker/data/nutrition/NutritionRules.kt
  - app/src/main/java/com/gymtracker/domain/LocalizedDecimal.kt
  - app/src/main/java/com/gymtracker/di/TimeModule.kt
  - app/src/main/java/com/gymtracker/data/dao/NutritionDao.kt
  - app/src/main/res/values/strings.xml
  - app/src/main/res/values-en/strings.xml
related_data: [FoodItem, FoodEntry, NutritionGoal, MealType]
tags: [gymtracker/feature, gymtracker/feature/nutrition]
---

# Feature — Beslenme

## Mevcut yetenek

Özel besin, öğün kaydı, kalori/makro toplamı ve günlük hedef. UI önceki gün, sonraki gün ve bugün arasında dolaşır; gelecek gün engellenir. Öğün miktarı ve öğün türü düzenlenebilir; silme Snackbar üzerinden geri alınabilir. Kalori aşımı, güvenli makro ilerlemesi, boş durum ve işlem hataları açık state olarak gösterilir.

## Mevcut akış

`SavedStateHandle` içindeki ISO `LocalDate` → `nutritionDayRange(date, Clock.zone)` → `flatMapLatest(getEntriesForDay)` + goal Flow → `DailyNutrition`. Takvim günü başlangıç/bitişi DST-safe hesaplanır. Bugünkü entry `Clock.instant()`, geçmiş gün entry’si seçilen günün yerel saatiyle yazılır.

Ondalık girdiler ortak `LocalizedDecimal` ile virgül/nokta destekli parse edilir. Besin miktarı, besin değerleri ve pozitif hedefler yazmadan önce doğrulanır; başarısız Room işlemi dialogu kapatmaz.

`FoodEntry` yazıldığı andaki besin adı ve makroları snapshot olarak taşır. Miktar düzenlemesi bu snapshot değerlerini eski/yeni gram oranıyla ölçekler; güncel `FoodItem` satırına dönüp geçmişi yeniden hesaplamaz. Silme undo’su aynı `id`, tarih, meal type ve snapshot ile satırı yeniden ekler. Karar: [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]].

Nutrition yüzeyi Türkçe ve İngilizce resource, cihaz-locale tarih biçimi ve birim formatlarını kullanır. Diğer ekranların İngilizce kapsamı tamamlanana kadar `MissingTranslation` lint suppression’ı bilinçli ve geçicidir.

## Kalan kapsam

- Food search DAO var, UI kullanmaz; recent/favorite quick-add yoktur.
- Seçili gün process recreation’da korunur; uygulama açıkken gece yarısı seçimi otomatik ilerlemez.
- Gerçek OS process-death testi ve tam uygulama İngilizce kapsamı bekliyor.

## Hedef durum

Recent/favorite food search, öğün kartında hızlı ekleme ve tam uygulama locale kapsamı. Home insert/update/delete Room Flow’una reaktiftir.

## Zorunlu testler

Uygulandı: 23/25 saat DST day range, goal 0 güvenliği, aşım görünümü, snapshot miktar ölçekleme, update/delete/aynı-ID undo Room sözleşmesi, Home update reaktivitesi, İngilizce resource ve light/dark/%200 font Compose/görsel kabulü. Kalan: gerçek process-death ve Snackbar UI interaction testi.

Bağlar: [[FEATURE_HOME]], [[ARCH_STATE_DATA_FLOW]], [[ARCH_DATABASE]], [[DESIGN_SYSTEM_GRAPH]].
