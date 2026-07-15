---
type: atlas
status: current
implementation_status: active
verified_commit: 341bf79
updated: 2026-07-15
tags: [gymtracker/workflow, gymtracker/source-map]
---

# Kaynak Dosya Atlası

## Build ve uygulama girişi

| Dosya | Sorumluluk | Bağ |
|---|---|---|
| `build.gradle.kts` | root plugin alias | [[ARCH_BUILD_DI]] |
| `settings.gradle.kts` | repositories + `:app` | [[ARCH_BUILD_DI]] |
| `gradle/libs.versions.toml` | dependency/plugin versions | [[ARCH_BUILD_DI]] |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 8.4 URL | [[ARCH_BUILD_DI]] |
| `app/build.gradle.kts` | Android/Compose/Hilt/Room config | [[ARCH_BUILD_DI]] |
| `AndroidManifest.xml` | permission/app/activity/backup | [[ARCH_BUILD_DI]] |
| `GymTrackerApp.kt` | Hilt application | [[ARCH_CURRENT_SYSTEM]] |
| `MainActivity.kt` | theme + scaffold + NavHost | [[ARCH_NAVIGATION]] |

## Veri

| Dosya | Sorumluluk | Kullanan |
|---|---|---|
| `data/entities/Entities.kt` | 9 Room entity + status/meal enum’ları | bütün feature’lar |
| `data/database/AppDatabase.kt` | DB v6 + converter + DAO registry | [[ARCH_DATABASE]] |
| `data/database/DatabaseMigrations.kt` | explicit 3→4→5→6 migration zinciri | [[ARCH_DATABASE]] |
| `data/dao/WorkoutDao.kt` | active/completed session, status-korumalı mutation ve completed-set load Flow | Home, Workout, Load |
| `data/dao/MeasurementDao.kt` | measurement queries | Home, Measurements |
| `data/dao/NutritionDao.kt` | food/entry/goal queries | Home, Nutrition |
| `data/dao/ProgressPhotoDao.kt` | photo queries | Photos |
| `data/dao/ProgramDao.kt` | program/day queries | Programs |
| `di/DatabaseModule.kt` | Room singleton + DAO providers | bütün ViewModel’ler |
| `di/TimeModule.kt` | dinamik sistem ZoneId kullanan injectable Clock | Nutrition/zaman kuralları |
| `data/home/HomeDashboardRepository.kt` | completed/active workout + measurement + nutrition Flow combine | Home |
| `data/workout/WorkoutRepository.kt` | tek aktif start/resume, detail ve status-korumalı mutation sınırı | Workout |
| `data/programs/ProgramRepository.kt` | program list/detail ve purchase sınırı | Programs |
| `data/photos/ProgressPhotoRepository.kt` | app-owned fotoğraf import/delete sınırı | Photos |

## Domain

| Dosya | Sorumluluk | Kritik bağ |
|---|---|---|
| `domain/MuscleGroup.kt` | 31 kas + 82 egzersiz/activation | Workout, Load, Programs |
| `domain/MuscleLoadCalculator.kt` | activation + recency + bodyweight fallback → kas yükü | Load |
| `data/workout/CompletedSetForLoad.kt` | completed-set DAO projeksiyonu | Load |
| `domain/ProgramExercise.kt` | program exercise + JSON | Programs |
| `domain/LocalizedDecimal.kt` | virgül/nokta destekli sonlu sayı parser’ı | Nutrition, Measurements |

## Navigation, tema ve component

| Dosya | Sorumluluk | Not |
|---|---|---|
| `ui/navigation/NavGraph.kt` | route sözleşmesi + 5 bottom item + navigation policy | [[ARCH_NAVIGATION]] |
| `ui/components/BottomNavigationBar.kt` | nested target seçimi + save/restore | [[ARCH_NAVIGATION]] |
| `ui/screens/progress/ProgressHubScreen.kt` | ölçüm/fotoğraf/kas yükü hub | [[FEATURE_PROGRESS_PHOTOS]] |
| `ui/components/MuscleBodyMap.kt` | bitmap overlay/hit test | [[FEATURE_MUSCLE_LOAD]] |
| `ui/theme/Theme.kt` | light/dark Material renk şeması + fatigue color | [[DESIGN_SYSTEM_GRAPH]] |
| `ui/theme/DesignTokens.kt` | type/spacing/shape/size tokenları | [[DESIGN_SYSTEM_GRAPH]] |
| `ui/components/GymTrackerComponents.kt` | metric/action/section/loading/empty/error ortak UI | [[DESIGN_SYSTEM_GRAPH]] |

## Home

| Dosya | Rol |
|---|---|
| `ui/screens/home/HomeViewModel.kt` | period tick + repository Flow → Loading/Content/Error |
| `data/home/HomeDashboardPeriod.kt` | ZoneId/DST-safe gün ve hafta sınırı |
| `ui/screens/home/HomeScreen.kt` | token tabanlı adaptive CTA/empty/metric/quick/last workout UI |

## Workout

| Dosya | Rol |
|---|---|
| `ui/screens/workout/WorkoutViewModel.kt` | active/completed list, SavedStateHandle detail ve mutation error state |
| `ui/screens/workout/WorkoutScreen.kt` | adaptive aktif/geçmiş listesi + salt-okunur veya editable ID detail |

## Kritik test dosyaları

| Dosya | Güvence |
|---|---|
| `data/database/DatabaseMigrationTest.kt` | v3→latest satır/status koruması |
| `data/workout/WorkoutRepositoryTest.kt` | eşzamanlı tek aktif + finish sonrası read-only |
| `ui/screens/workout/WorkoutScreenTest.kt` | aktif/geçmiş, read-only, light/dark ve %200 font |

## Nutrition

| Dosya | Rol |
|---|---|
| `ui/screens/nutrition/NutritionViewModel.kt` | selected day + totals + CRUD |
| `data/nutrition/NutritionRules.kt` | DST-safe day range, entry time, goal/food/progress validation |
| `ui/screens/nutrition/NutritionScreen.kt` | tarih pager + adaptive summary/macro/meal/form/goal UI |

## Measurements

| Dosya | Rol |
|---|---|
| `data/measurements/MeasurementRules.kt` | alan aralıkları, boş kayıt engeli ve kilo trendi |
| `ui/screens/measurements/MeasurementsViewModel.kt` | latest/history/trend + validation/save/delete |
| `ui/screens/measurements/MeasurementsScreen.kt` | adaptive latest/delta/history/empty/form UI |

## Progress photos

| Dosya | Rol |
|---|---|
| `ui/screens/progress/ProgressViewModel.kt` | Fotoğraf doğrulama, kayıt/silme, typed mesajlar ve storage reconciliation |
| `data/photos/ProgressPhotoRules.kt` | opsiyonel kilo/not validation |
| `data/photos/PhotoFileStorage.kt` | atomik kopya + limit + managed reconciliation |
| `ui/screens/progress/ProgressScreen.kt` | picker/adaptive filter-grid/viewer/legacy UI |

## Programs

| Dosya | Rol |
|---|---|
| `ui/screens/programs/ProgramsViewModel.kt` | SavedStateHandle ID + repository list/detail state |
| `ui/screens/programs/ProgramsScreen.kt` | route tabanlı list/detail/purchase/workout cards |

## Muscle load

| Dosya | Rol |
|---|---|
| `ui/screens/fatigue/FatigueViewModel.kt` | completed-set Flow + takvim rollover → load state |
| `ui/screens/fatigue/FatigueScreen.kt` | front/back map + methodology + accessible list |

## Kaynaklar

- `res/drawable-nodpi/body_*.png`: 2 × 470×836 body image
- `res/drawable-nodpi/mask_*.png`: 38 kullanılan maske
- `res/values/strings.xml`: app name + Home/navigation/Workout/Nutrition/Measurements metinleri
- `res/values/themes.xml`, `res/values-night/themes.xml`: light/dark launch/platform theme
- `res/xml/backup_rules.xml`, `data_extraction_rules.xml`: progress fotoğraflarını backup/transfer dışında tutar

Dosya yolu ararken önce bu atlas, sonra ilgili feature node içindeki `source_files` kullanılmalıdır.
