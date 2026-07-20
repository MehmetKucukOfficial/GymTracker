---
type: risk-register
status: current
implementation_status: current
verified_commit: 81cb7fe
updated: 2026-07-20
tags: [gymtracker/risk, gymtracker/quality]
---

# Risk Kayıt Grafı

| ID | Seviye | Durum | Risk | Etkilenen düğüm | Çözüm kapısı |
|---|---|---|---|---|---|
| R1 | P0 | resolved (v3+) | Destructive migration veri siler | [[ARCH_DATABASE]] | [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]] |
| R2 | P0 | mitigated+++ | Yeni fotoğraf URI’si, bounded decode/EXIF/thumbnail, iki dosyalı rollback, 48 MB low-space preflight, XML backup politikası, metadata-only restore reconciliation, gerçek 120-row host reboot ve kimlik koruyan legacy v0/v1 yükseltme çözüldü; gerçek Android backup transport restore deneyi açık | [[FEATURE_PROGRESS_PHOTOS]] | [[ADR_004_PERSISTENT_PHOTO_STORAGE]] |
| R3 | P0/P1 | resolved-core++++ | Linux wrapper, tek `ciQuality`, minified release ve KVM hızlandırmalı geniş API 34 app/reboot/benchmark/process-death akışı PR24 hosted koşusu `29775858318` üzerinde yeşil; artifact XML 80+5, reboot 1+1 ve beş Perfetto trace doğrulandı. Paket-kill harness'i resumed-task kontrolü/bounded retry/task-korumalı fallback ile geçti. Branch-protection özel depo/hesap planı nedeniyle yönetim takibi | [[ARCH_BUILD_DI]] | hosted CI + yönetim takibi |
| R4 | P1 | resolved | Program seed duplicate/yarım | [[FEATURE_PROGRAMS]] | unique + transaction test |
| R5 | P1 | resolved | Home reaktif loading/content/error akışı ile Kas Yükü ortak injectable Clock kullanır; yerel gece yarısı ve runtime zone-change yeniden hesaplaması deterministik testlidir | [[FEATURE_HOME]] | shared period Flow + Clock/ZoneId test |
| R6 | P1 | resolved | Program purchase detail ve stale local selection | [[FEATURE_PROGRAMS]] | ID route/reactive detail |
| R7 | P1 | resolved | Workout detail argümanı kullanılmaz | [[FEATURE_WORKOUT]] | [[ARCH_NAVIGATION]] |
| R8 | P1 | resolved (v6) | Legacy workout `COMPLETED` migrate edilir; geçmiş detail salt okunurdur | [[FEATURE_WORKOUT]] | [[ADR_006_WORKOUT_SESSION_LIFECYCLE]] |
| R9 | P1 | resolved+ | Nutrition DST-safe takvim aralığı, pozitif hedef, sonlu progress, bozuk restore fallback’i ve seçili tarihin gerçek process-death restorasyonu uygulandı | [[FEATURE_NUTRITION]] | Clock + validation + PID/state test |
| R10 | P1 | mitigated-core+ | UI kas yükü/seviye/skor olarak adlandırıldı; formül açık ve sıfır kilolu setler son ölçümle kalibre edilir. Stable-ID load projeksiyonu, hareket katsayısı ve kişisel hacim baseline’ı açık | [[FEATURE_MUSCLE_LOAD]] | [[ADR_005_MUSCLE_LOAD_NAMING]] |
| R11 | P1 | mitigated-core | Workout, Nutrition, Measurements ve Programs input/mutation doğrulandı; Nutrition/Measurements edit-delete-undo kimliği ve snapshot’ı testli | bütün feature’lar | shared validator + [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]] |
| R12 | P2 | resolved-core (v7) | Program aktif okuma modeli ilişkisel; JSON yalnız kayıpsız legacy fallback ve bozuk payload UI’da güvenli boş state | [[ARCH_DATABASE]] | [[ADR_007_RELATIONAL_PROGRAM_PLANS]] |
| R13 | P2 | resolved++++ | 5 hedefli compact bottom bar, 600 dp+ rail, 840 dp+ Workout/Programs liste–detay ve doğrulanmış dış deep-link URI’leri aynı route sahipliği/pozitif-ID/save-restore sözleşmesini kullanıyor; ayrı süreçte PID/task yanında Nutrition tarihi ve Measurements checked period restorasyonu da kapsanır | [[ARCH_NAVIGATION]] | [[ADR_002_FIVE_TARGET_NAVIGATION]] |
| R14 | P2 | resolved-core | Permission azaltıldı; hassas progress fotoğrafları legacy/modern cloud backup ve device transfer dışında; üç dışlama XML parser testleriyle doğrulanır | [[ARCH_BUILD_DI]] | permission + extraction rules |
| R15 | P2 | mitigated+++ | Uygulama geneli 385/385 TR/EN string kaynağı, adaptive shell, 840 dp+ Workout/Programs list-detail, ortak büyük-font top bar/Snackbar, Settings export loading/success/error, selected semantics ve altı kalıcı golden aktif; Photos/Load/Settings golden ve genel disabled/loading yayılımı açık | [[DESIGN_SYSTEM_GRAPH]] | design DoD |
| R16 | P1 | mitigated-config | Protected environment, fail-fast secrets, geçici keystore, APK/AAB imza doğrulaması ve artifact kapısı tanımlı; gerçek production secret + Play internal-track smoke’u açık | [[ADR_009_SECRET_BACKED_RELEASES]] | production environment + release smoke |
| R17 | P1 | partial+ | Baseline/Startup Profile ile Home/Workout ve 120-thumbnail grid macrobenchmark CUJ’ları üretildi; grid JSON + beş Perfetto trace CI artifact sözleşmesine eklendi. Emülatör yalnız karşılaştırma baseline’ı, fiziksel referans cihaz kabulü yapılmadı | [[PERFORMANCE_BASELINE]] | physical release benchmark |
| R18 | P1 | partial+ | Manuel format v1 export; tek transaction snapshot, sürüm header'ı, yönetilen medya, legacy metadata-only, path/alan/temp güvenliği ve gerçek SAF dosya testiyle aktif. Staged doğrulamalı atomik restore ve güçlü onaylı tüm-veri silme henüz uygulanmadı; arşiv başarı metni restore garantisi değildir | [[FEATURE_SETTINGS_DATA]] | [[ADR_010_VERSIONED_MANUAL_DATA_PORTABILITY]] + restore/delete acceptance |

Aktif risk durumu [[SESSION_HANDOFF]] içinde tutulur; çözülen risk silinmez, `resolved` ve commit ile işaretlenir.
