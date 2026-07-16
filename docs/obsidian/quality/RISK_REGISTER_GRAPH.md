---
type: risk-register
status: current
implementation_status: current
verified_commit: 5c3ce35
updated: 2026-07-16
tags: [gymtracker/risk, gymtracker/quality]
---

# Risk Kayıt Grafı

| ID | Seviye | Durum | Risk | Etkilenen düğüm | Çözüm kapısı |
|---|---|---|---|---|---|
| R1 | P0 | resolved (v3+) | Destructive migration veri siler | [[ARCH_DATABASE]] | [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]] |
| R2 | P0 | mitigated-core | Yeni fotoğraf URI’si, bounded decode/EXIF/thumbnail, iki dosyalı rollback, backup tutarlılığı ve orphan/missing reconciliation çözüldü; legacy v0 yeniden seçim ve düşük alan/reboot kabulü açık | [[FEATURE_PROGRESS_PHOTOS]] | [[ADR_004_PERSISTENT_PHOTO_STORAGE]] |
| R3 | P0/P1 | partial | Wrapper/test/lint düzeldi; CI açık | [[ARCH_BUILD_DI]] | PR 1–2 |
| R4 | P1 | resolved | Program seed duplicate/yarım | [[FEATURE_PROGRAMS]] | unique + transaction test |
| R5 | P1 | mitigated+ | Home reaktif, rollover ve loading/error uygulandı; Clock/zone-change testi açık | [[FEATURE_HOME]] | Flow combine + period tick + UiState |
| R6 | P1 | resolved | Program purchase detail ve stale local selection | [[FEATURE_PROGRAMS]] | ID route/reactive detail |
| R7 | P1 | resolved | Workout detail argümanı kullanılmaz | [[FEATURE_WORKOUT]] | [[ARCH_NAVIGATION]] |
| R8 | P1 | resolved (v6) | Legacy workout `COMPLETED` migrate edilir; geçmiş detail salt okunurdur | [[FEATURE_WORKOUT]] | [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] |
| R9 | P1 | resolved | Nutrition DST-safe takvim aralığı, pozitif hedef ve sonlu progress uygulandı | [[FEATURE_NUTRITION]] | Clock + validation + unit/UI test |
| R10 | P1 | mitigated-core+ | UI kas yükü/seviye/skor olarak adlandırıldı; formül açık ve sıfır kilolu setler son ölçümle kalibre edilir. Stable-ID load projeksiyonu, hareket katsayısı ve kişisel hacim baseline’ı açık | [[FEATURE_MUSCLE_LOAD]] | [[ADR_005_MUSCLE_LOAD_NAMING]] |
| R11 | P1 | mitigated-core | Workout, Nutrition, Measurements ve Programs input/mutation doğrulandı; Nutrition/Measurements edit-delete-undo kimliği ve snapshot’ı testli | bütün feature’lar | shared validator + [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]] |
| R12 | P2 | resolved-core (v7) | Program aktif okuma modeli ilişkisel; JSON yalnız kayıpsız legacy fallback ve bozuk payload UI’da güvenli boş state | [[ARCH_DATABASE]] | [[ADR_007_RELATIONAL_PROGRAM_PLANS]] |
| R13 | P2 | resolved (phone) | 7 öğeli navigation; adaptive rail bekliyor | [[ARCH_NAVIGATION]] | [[ADR_002_FIVE_TARGET_NAVIGATION]] |
| R14 | P2 | mitigated+ | Permission azaltıldı; hassas progress fotoğrafları cloud backup/device transfer dışında | [[ARCH_BUILD_DI]] | permission + extraction rules |
| R15 | P2 | mitigated-core+ | Çekirdek yüzeyler string, semantics, light/dark ve %200 font kullanıyor; Nutrition/Measurements İngilizce kapsamı ve plural/birim biçimi var; app-geneli ikinci locale/adaptive/golden açık | [[DESIGN_SYSTEM_GRAPH]] | design DoD |

Aktif risk durumu [[SESSION_HANDOFF]] içinde tutulur; çözülen risk silinmez, `resolved` ve commit ile işaretlenir.
