---
type: moc
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-14
tags: [gymtracker/moc, gymtracker/data]
---

# MOC — Veri

## Room çekirdeği

- [[ARCH_DATABASE]] — tam ER ve DAO grafı
- Kaynak: `data/entities/Entities.kt`
- DB: `AppDatabase`, sürüm 3, 9 entity
- DI: `DatabaseModule`, 5 DAO provider

## Veri kullanan feature’lar

- Workout tabloları → [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[FEATURE_MUSCLE_LOAD]]
- Measurement → [[FEATURE_MEASUREMENTS]], [[FEATURE_HOME]]
- Food → [[FEATURE_NUTRITION]], [[FEATURE_HOME]]
- ProgressPhoto → [[FEATURE_PROGRESS_PHOTOS]]
- Program → [[FEATURE_PROGRAMS]]

## P0 veri işleri

1. [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]] — destructive migration kaldır
2. [[ADR_004_PERSISTENT_PHOTO_STORAGE]] — kalıcı dosya/izin
3. Program seed’i unique + transaction yap
4. Tarih index ve `Clock/ZoneId` standardı ekle

## Veri değişikliğinde zorunlu okuma

[[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]] ve [[TEST_MATRIX#Room ve migration]].
