---
type: atlas
status: current
implementation_status: active
verified_commit: a2586d1
updated: 2026-07-16
tags: [gymtracker/workflow, gymtracker/source-map]
---

# Kaynak Dosya Atlası

## Build ve uygulama girişi

| Dosya | Sorumluluk | Bağ |
|---|---|---|
| `.github/workflows/android-ci.yml` | PR/main quality + API 34 device/golden CI ve artifact’ler | [[ARCH_BUILD_DI]] |
| `.github/workflows/android-release.yml` | protected production secret’larıyla signed APK/AAB üretim ve doğrulama | [[ADR_009_SECRET_BACKED_RELEASES]] |
| `build.gradle.kts` | root plugin alias + tek `ciQuality` kapısı | [[ARCH_BUILD_DI]] |
| `settings.gradle.kts` | repositories + `:app` + `:baselineprofile` | [[ARCH_BUILD_DI]] |
| `gradle/libs.versions.toml` | dependency/plugin versions | [[ARCH_BUILD_DI]] |
| `gradle/wrapper/gradle-wrapper.properties`, `gradlew` | Gradle 8.4 URL + Linux çalıştırılabilir wrapper | [[ARCH_BUILD_DI]] |
| `app/build.gradle.kts` | Android/Compose/Hilt/Room + minified release, signing/profile doğrulama görevleri | [[ARCH_BUILD_DI]] |
| `baselineprofile/` | startup/Home scroll/Workout profile producer ve macrobenchmark’lar | [[PERFORMANCE_BASELINE]] |
| `app/src/release/generated/baselineProfiles/` | ölçülerek üretilen release Baseline + Startup Profile | [[PERFORMANCE_BASELINE]] |
| `app/proguard-rules.pro` | yalnız kanıtla eklenecek proje R8 istisnaları | [[ARCH_BUILD_DI]] |
| `AndroidManifest.xml` | permission/app/activity/backup | [[ARCH_BUILD_DI]] |
| `GymTrackerApp.kt` | Hilt application | [[ARCH_CURRENT_SYSTEM]] |
| `MainActivity.kt` | theme + compact bottom-bar / 600 dp+ rail adaptive scaffold + NavHost | [[ARCH_NAVIGATION]] |

## Veri

| Dosya | Sorumluluk | Kullanan |
|---|---|---|
| `data/entities/Entities.kt` | Room entity + v7 Programs + v8 measurement index + v9 photo media metadata | bütün feature’lar |
| `data/database/AppDatabase.kt` | DB v9 + converter + DAO registry | [[ARCH_DATABASE]] |
| `data/database/DatabaseMigrations.kt` | explicit 3→4→5→6→7→8→9 migration zinciri | [[ARCH_DATABASE]] |
| `data/dao/WorkoutDao.kt` | active/completed session, planned-set/status mutation ve completed-set load Flow | Home, Workout, Programs, Load |
| `data/dao/MeasurementDao.kt` | all/latest/indexed period + latest valid weight Flow + mutation queries | Home, Measurements, Load |
| `data/dao/NutritionDao.kt` | food/entry/goal + update/delete/restore queries | Home, Nutrition |
| `data/dao/ProgressPhotoDao.kt` | photo queries | Photos |
| `data/dao/ProgramDao.kt` | program + ilişkisel day/exercise queries | Programs |
| `di/DatabaseModule.kt` | Room singleton + DAO providers | bütün ViewModel’ler |
| `di/TimeModule.kt` | dinamik sistem ZoneId kullanan injectable Clock | Nutrition/zaman kuralları |
| `data/home/HomeDashboardRepository.kt` | completed/active workout + measurement + nutrition Flow combine | Home |
| `data/workout/WorkoutRepository.kt` | tek aktif start/resume, planned-set completion ve status-korumalı mutation | Workout, Programs |
| `data/programs/ProgramRepository.kt` | list/detail/demo erişim + atomic program→workout sınırı | Programs |
| `data/programs/ProgramWorkoutPlan.kt` | Room relation modeli ve sıralı egzersiz görünümü | Programs |
| `data/measurements/MeasurementRepository.kt` | all/period Flow ve measurement mutation sınırı | Measurements |
| `data/photos/ProgressPhotoRepository.kt` | storage v2 medya import/delete/reconciliation sınırı | Photos |

## Domain

| Dosya | Sorumluluk | Kritik bağ |
|---|---|---|
| `domain/MuscleGroup.kt` | 32 kas + 82 egzersiz/activation | Workout, Load, Programs |
| `domain/MuscleLoadCalculator.kt` | activation + recency + kişisel bodyweight/fallback → kas yükü | Load |
| `data/workout/CompletedSetForLoad.kt` | completed-set DAO projeksiyonu | Load |
| `domain/ProgramExercise.kt` | legacy JSON migration modeli + locale-independent stable ID | Programs, Workout |
| `domain/LocalizedDecimal.kt` | virgül/nokta destekli sonlu sayı parser’ı | Nutrition, Measurements |

## Navigation, tema ve component

| Dosya | Sorumluluk | Not |
|---|---|---|
| `ui/navigation/NavGraph.kt` | route sözleşmesi + 5 bottom item + navigation policy | [[ARCH_NAVIGATION]] |
| `ui/components/BottomNavigationBar.kt` | bottom bar/rail, 600 dp politikası, nested target seçimi + save/restore | [[ARCH_NAVIGATION]] |
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
| `ui/screens/nutrition/NutritionViewModel.kt` | selected day + totals + snapshot edit/delete/undo |
| `data/nutrition/NutritionRules.kt` | DST-safe day range, validation ve snapshot miktar ölçekleme |
| `ui/screens/nutrition/NutritionScreen.kt` | locale tarih + adaptive summary/macro/meal/edit/undo UI |

## Measurements

| Dosya | Rol |
|---|---|
| `data/measurements/MeasurementRules.kt` | alan aralıkları, trend ve 7/30/90/tümü tarih aralığı |
| `ui/screens/measurements/MeasurementsViewModel.kt` | latest/history/trend/chart period + edit/delete/undo |
| `ui/screens/measurements/MeasurementsScreen.kt` | adaptive latest/chart/history/custom-date/edit/undo UI |

## Progress photos

| Dosya | Rol |
|---|---|
| `ui/screens/progress/ProgressViewModel.kt` | Fotoğraf doğrulama, kayıt/silme, typed mesajlar ve storage reconciliation |
| `data/photos/ProgressPhotoRules.kt` | opsiyonel kilo/not validation |
| `data/photos/PhotoFileStorage.kt` | bounded decode + EXIF + 2048 ana/512 thumbnail + atomik publish/reconciliation |
| `ui/screens/progress/ProgressScreen.kt` | picker/adaptive thumbnail grid/ana viewer/legacy UI |

## Programs

| Dosya | Rol |
|---|---|
| `ui/screens/programs/ProgramsViewModel.kt` | SavedStateHandle ID + list/detail/start UiState ve typed mesaj |
| `ui/screens/programs/ProgramsScreen.kt` | adaptive list/detail/demo erişim/ilişkisel plan/start UI |

## Muscle load

| Dosya | Rol |
|---|---|
| `ui/screens/fatigue/FatigueViewModel.kt` | completed-set + latest-weight Flow + takvim rollover → calibrated load state |
| `ui/screens/fatigue/FatigueScreen.kt` | front/back map + calibration source + methodology + accessible list |

## Kaynaklar

- `res/drawable-nodpi/body_*.png`: 2 × 470×836 body image
- `res/drawable-nodpi/mask_*.png`: 38 kullanılan maske
- `res/values/strings.xml`, `res/values-en/strings.xml`: uygulama geneli eş anahtarlı Türkçe/English string ve plural katalogları
- `androidTest/assets/goldens/`, `ui/golden/*`: API 34 kalıcı pixel-golden baseline ve karşılaştırıcı
- `res/values/themes.xml`, `res/values-night/themes.xml`: light/dark launch/platform theme
- `res/xml/backup_rules.xml`, `data_extraction_rules.xml`: progress fotoğraflarını backup/transfer dışında tutar

Dosya yolu ararken önce bu atlas, sonra ilgili feature node içindeki `source_files` kullanılmalıdır.
