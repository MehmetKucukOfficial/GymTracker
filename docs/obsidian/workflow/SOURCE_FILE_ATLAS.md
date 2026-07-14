---
type: atlas
status: current
implementation_status: active
verified_commit: 4d0cad8
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
| `data/entities/Entities.kt` | 9 Room entity + enum | bütün feature’lar |
| `data/database/AppDatabase.kt` | DB v5 + converter + DAO registry | [[ARCH_DATABASE]] |
| `data/dao/WorkoutDao.kt` | session/set queries | Home, Workout, Load |
| `data/dao/MeasurementDao.kt` | measurement queries | Home, Measurements |
| `data/dao/NutritionDao.kt` | food/entry/goal queries | Home, Nutrition |
| `data/dao/ProgressPhotoDao.kt` | photo queries | Photos |
| `data/dao/ProgramDao.kt` | program/day queries | Programs |
| `di/DatabaseModule.kt` | Room singleton + DAO providers | bütün ViewModel’ler |
| `data/home/HomeDashboardRepository.kt` | workout/measurement/nutrition Flow combine | Home |
| `data/workout/WorkoutRepository.kt` | workout list/detail ve mutation sınırı | Workout |
| `data/programs/ProgramRepository.kt` | program list/detail ve purchase sınırı | Programs |
| `data/photos/ProgressPhotoRepository.kt` | app-owned fotoğraf import/delete sınırı | Photos |

## Domain

| Dosya | Sorumluluk | Kritik bağ |
|---|---|---|
| `domain/MuscleGroup.kt` | 31 kas + 82 egzersiz/activation | Workout, Load, Programs |
| `domain/FatigueCalculator.kt` | hacim→kas skor hesabı | Load |
| `domain/ProgramExercise.kt` | program exercise + JSON | Programs |

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
| `ui/screens/workout/WorkoutViewModel.kt` | SavedStateHandle ID + repository list/detail state |
| `ui/screens/workout/WorkoutScreen.kt` | list + tam ekran ID detail/dialog/set UI |

## Nutrition

| Dosya | Rol |
|---|---|
| `ui/screens/nutrition/NutritionViewModel.kt` | selected day + totals + CRUD |
| `ui/screens/nutrition/NutritionScreen.kt` | summary/meal/form/goal UI |

## Measurements

| Dosya | Rol |
|---|---|
| `ui/screens/measurements/MeasurementsViewModel.kt` | list/save/delete |
| `ui/screens/measurements/MeasurementsScreen.kt` | latest/history/form UI |

## Progress photos

| Dosya | Rol |
|---|---|
| `ui/screens/progress/ProgressViewModel.kt` | URI metadata save/delete |
| `ui/screens/progress/ProgressScreen.kt` | picker/filter/grid/dialog |

## Programs

| Dosya | Rol |
|---|---|
| `ui/screens/programs/ProgramsViewModel.kt` | SavedStateHandle ID + repository list/detail state |
| `ui/screens/programs/ProgramsScreen.kt` | route tabanlı list/detail/purchase/workout cards |

## Muscle load

| Dosya | Rol |
|---|---|
| `ui/screens/fatigue/FatigueViewModel.kt` | son 7 gün tek sefer load |
| `ui/screens/fatigue/FatigueScreen.kt` | front/back map + selection |

## Kaynaklar

- `res/drawable-nodpi/body_*.png`: 2 × 470×836 body image
- `res/drawable-nodpi/mask_*.png`: 38 kullanılan maske
- `res/values/strings.xml`: app name + Home/navigation metinleri
- `res/values/themes.xml`, `res/values-night/themes.xml`: light/dark launch/platform theme

Dosya yolu ararken önce bu atlas, sonra ilgili feature node içindeki `source_files` kullanılmalıdır.
