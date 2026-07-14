---
type: risk-register
status: current
implementation_status: current
verified_commit: f9d6d75
updated: 2026-07-15
tags: [gymtracker/risk, gymtracker/quality]
---

# Risk Kayıt Grafı

| ID | Seviye | Durum | Risk | Etkilenen düğüm | Çözüm kapısı |
|---|---|---|---|---|---|
| R1 | P0 | resolved (v3+) | Destructive migration veri siler | [[ARCH_DATABASE]] | [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]] |
| R2 | P0 | mitigated | Yeni fotoğraflarda URI kaybı çözüldü; legacy v0 ve backup politikası açık | [[FEATURE_PROGRESS_PHOTOS]] | [[ADR_004_PERSISTENT_PHOTO_STORAGE]] |
| R3 | P0/P1 | partial | Wrapper/test/lint düzeldi; CI açık | [[ARCH_BUILD_DI]] | PR 1–2 |
| R4 | P1 | resolved | Program seed duplicate/yarım | [[FEATURE_PROGRAMS]] | unique + transaction test |
| R5 | P1 | mitigated+ | Home reaktif, rollover ve loading/error uygulandı; Clock/zone-change testi açık | [[FEATURE_HOME]] | Flow combine + period tick + UiState |
| R6 | P1 | resolved | Program purchase detail ve stale local selection | [[FEATURE_PROGRAMS]] | ID route/reactive detail |
| R7 | P1 | resolved | Workout detail argümanı kullanılmaz | [[FEATURE_WORKOUT]] | [[ARCH_NAVIGATION]] |
| R8 | P1 | resolved (v6) | Legacy workout `COMPLETED` migrate edilir; geçmiş detail salt okunurdur | [[FEATURE_WORKOUT]] | [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] |
| R9 | P1 | resolved | Nutrition DST-safe takvim aralığı, pozitif hedef ve sonlu progress uygulandı | [[FEATURE_NUTRITION]] | Clock + validation + unit/UI test |
| R10 | P1 | open | Kas metriği yanlış kesinlik | [[FEATURE_MUSCLE_LOAD]] | load naming/methodology |
| R11 | P1 | partial+ | Workout, Nutrition ve Measurements input/mutation doğrulandı; kalan feature’lar açık | bütün feature’lar | shared validator |
| R12 | P2 | open | Program JSON kırılgan | [[ARCH_DATABASE]] | relational migration |
| R13 | P2 | resolved (phone) | 7 öğeli navigation; adaptive rail bekliyor | [[ARCH_NAVIGATION]] | [[ADR_002_FIVE_TARGET_NAVIGATION]] |
| R14 | P2 | partial | Permission azaltıldı; backup gizliliği açık | [[ARCH_BUILD_DI]] | permission + extraction rules |
| R15 | P2 | partial++ | Home/nav, Workout, Nutrition ve Measurements string, semantics, light/dark ve %200 font düzeldi; diğer ekranlar açık | [[DESIGN_SYSTEM_GRAPH]] | design DoD |

Aktif risk durumu [[SESSION_HANDOFF]] içinde tutulur; çözülen risk silinmez, `resolved` ve commit ile işaretlenir.
