---
type: architecture
status: current
implementation_status: current
verified_commit: 7917358
updated: 2026-07-15
source_files:
  - app/src/main/java/com/gymtracker/data/entities/Entities.kt
  - app/src/main/java/com/gymtracker/data/database/AppDatabase.kt
  - app/src/main/java/com/gymtracker/data/database/DatabaseMigrations.kt
  - app/src/main/java/com/gymtracker/data/dao/WorkoutDao.kt
  - app/src/main/java/com/gymtracker/data/dao/MeasurementDao.kt
  - app/src/main/java/com/gymtracker/data/dao/NutritionDao.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgressPhotoDao.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgramDao.kt
tags: [gymtracker/architecture, gymtracker/data]
---

> Update (2026-07-14, v5): `ProgressPhoto` için `mimeType`, `sizeBytes` ve `storageVersion` kolonları eklendi. `MIGRATION_4_5`, v3→v5 fixture koruma testi ve v3/v4/v5 schema JSON’ları kaynak kontrolündedir. Legacy fotoğraflar `storageVersion=0`, yeni app-owned importlar `storageVersion=1` olur.

> Update (2026-07-15, v6): `WorkoutSession.status`, nullable `completedAt` ve status index’i eklendi. `MIGRATION_5_6` legacy workout’ları veri kaybetmeden `COMPLETED` yapar; v3→v6 fixture ve repository transaction testleri API 34’te geçer. Karar: [[ADR_006_WORKOUT_SESSION_LIFECYCLE]].

> Update (2026-07-15, v7): `ProgramDayExercise`, `WorkoutSet.exerciseId` ve nullable `targetReps` eklendi. `MIGRATION_6_7` geçerli program JSON’unu ilişkisel satırlara taşır, bozuk payload’ı kayıpsız legacy fallback olarak korur. Program→ACTIVE workout copy ve planlanan-set completion transaction/state sözleşmesi [[ADR_007_RELATIONAL_PROGRAM_PLANS]] ile aktiftir.

> Update (2026-07-15, v8): `body_measurements(date)` index’i explicit `MIGRATION_7_8` ile eklendi. v7 measurement fixture’ı, index varlığı ve v3/v6→latest zinciri API 34 migration testleriyle doğrulanır. Period query `MeasurementRepository` üzerinden 7/30/90/tümü grafiğini besler.

> Implementation update (2026-07-14): `exportSchema=true`; v3 schema hash `bb80650678afbc615c5dec81809ab042` is stored under `app/schemas`; `fallbackToDestructiveMigration()` is removed; migrations are registered through `DatabaseMigrations.ALL`. [[DatabaseMigrationTest]] and [[WorkoutDaoTest]] pass on the API 34 emulator. v3 is the minimum evidence-backed migration baseline.

# Mimari — Room Veritabanı

## Mevcut ER

```mermaid
erDiagram
    WorkoutSession ||--o{ WorkoutSet : CASCADE
    Program ||--o{ ProgramWorkout : CASCADE
    ProgramWorkout ||--o{ ProgramDayExercise : CASCADE
    FoodItem ||..o{ FoodEntry : "logical id, no FK"
    WorkoutSession {
      long id PK
      string name
      long date
      int durationMinutes
      string status
      long completedAt
    }
    WorkoutSet {
      long id PK
      long sessionId FK
      string exerciseName
      int setNumber
      int reps
      float weightKg
      string exerciseId
      string targetReps
      int exerciseOrder
    }
    BodyMeasurement {
      long id PK
      long date INDEX
      float weightKg
    }
    FoodItem { long id PK }
    FoodEntry { long id PK }
    ProgressPhoto {
      long id PK
      string photoUri
      string mimeType
      long sizeBytes
      int storageVersion
    }
    Program { long id PK }
    ProgramWorkout { long id PK }
    ProgramDayExercise {
      long id PK
      long programWorkoutId FK
      string exerciseId
      int position
      int targetSets
      string targetReps
    }
    NutritionGoal { int id PK }
```

## DAO sahipliği

| DAO | Tablolar | Feature |
|---|---|---|
| WorkoutDao | sessions, sets | [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[FEATURE_MUSCLE_LOAD]] |
| MeasurementDao | measurements + indexed period query | [[FEATURE_MEASUREMENTS]], [[FEATURE_HOME]] |
| NutritionDao | food items/entries/goals | [[FEATURE_NUTRITION]], [[FEATURE_HOME]] |
| ProgressPhotoDao | photos | [[FEATURE_PROGRESS_PHOTOS]] |
| ProgramDao | programs/workouts | [[FEATURE_PROGRAMS]] |

## Kritik mevcut gerçekler

- DB version 8, `exportSchema=true`; v3–v8 schema JSON’ları version control altında.
- Destructive fallback kaldırıldı; bilinmeyen sürümler veri silmek yerine fail-closed davranır.
- `DatabaseMigrations.ALL`, 3→4→5→6→7→8 kenarlarını kaydeder; v3→latest satır koruma/status, v6 JSON normalizasyon ve v7 measurement index testleri API 34’te geçer.
- Workout yazma yolu tek transaction içinde start-or-resume uygular; set/finish mutation’ları `status='ACTIVE'` koşuluyla korunur.
- Programların aktif okuma modeli `ProgramWorkout → ProgramDayExercise` ilişkisidir. `ProgramWorkout.exercises` yalnız v6 migration fallback payload’ıdır; UI parse etmez.
- `body_measurements(date)` indexlidir; diğer tarih sorgularının query-plan/index değerlendirmesi açıktır.
- `WorkoutDao.getSetsSince()` içindeki `date(sessionId)` semantik olarak yanlış ve kullanılmıyor.
- Program seed tek Room transaction’ı ve `seed_metadata` claim’i ile idempotenttir.
- Fotoğraf byte’ları Room içinde BLOB değildir; app internal storage’da, DB’de URI ve metadata tutulur.

## Hedef değişiklikler

- [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]]
- Workout için `startedAt` adlandırmasını netleştirme, exerciseId/position ve gerekirse DB seviyesinde partial unique active index
- Legacy program JSON kolonunu kanıtlı destek penceresi sonrasında kaldırma
- Program seedKey/contentVersion unique
- Photo thumbnail/dimension/media-transform metadata ve legacy re-import UX
- Tarih ve compound index’ler
- Para minor unit/currency

Her değişiklikte [[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]] uygulanır.
