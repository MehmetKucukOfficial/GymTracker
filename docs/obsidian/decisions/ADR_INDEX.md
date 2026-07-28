---
type: adr-index
status: current
implementation_status: mixed
updated: 2026-07-20
tags: [gymtracker/decision, gymtracker/moc]
---

# ADR İndeksi

| ADR | Durum | Kod durumu | Etki |
|---|---|---|---|
| [[ADR_001_LOCAL_FIRST_SINGLE_MODULE]] | accepted | current | altyapı kapsamı |
| [[ADR_002_FIVE_TARGET_NAVIGATION]] | accepted | current (phone) | bütün navigation/UI |
| [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]] | accepted | current | bütün kalıcı veri |
| [[ADR_004_PERSISTENT_PHOTO_STORAGE]] | accepted | current (Room v9/storage v2) | fotoğraf + backup + medya hattı |
| [[ADR_005_MUSCLE_LOAD_NAMING]] | accepted | current-calibration | hesap + measurement + UI dili |
| [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] | accepted | current | Workout + Home + Room v6 |
| [[ADR_007_RELATIONAL_PROGRAM_PLANS]] | accepted | current | Programs + Workout + Room v7 |
| [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]] | accepted | current | Nutrition + Measurements edit/undo |
| [[ADR_009_SECRET_BACKED_RELEASES]] | accepted | current-config | production signing + distribution |
| [[ADR_010_VERSIONED_MANUAL_DATA_PORTABILITY]] | accepted | current | Room + Settings + progress photo export, strict staged restore/rollback and strong-confirm deletion |

Yeni karar [[TEMPLATE_ADR]] ile eklenir. Karar değişirse eski ADR silinmez; superseded işaretlenip yeni ADR’ye bağlanır.
