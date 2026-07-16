---
type: roadmap
status: current
implementation_status: active
verified_commit: e28a22d
updated: 2026-07-16
tags: [gymtracker/roadmap, gymtracker/workflow]
---

# Yol Haritası — Uygulama Grafı

## PR bağımlılıkları

```mermaid
flowchart TD
    PR1[PR 1 Build hygiene] --> PR2[PR 2 Test foundation]
    PR2 --> PR3[PR 3 Database safety]
    PR3 --> PR4[PR 4 Seed safety]
    PR3 --> PR5[PR 5 Photo storage]
    PR4 --> PR6[PR 6 Navigation + state]
    PR5 --> PR6
    PR6 --> UI[Design system + Home]
    UI --> WO[Workout vertical slice]
    WO --> N[PR 9 Nutrition + Measurements]
    N --> P[PR 10 Photos + Load]
    P --> PG[PR 11 Programs]
    PG --> ND[PR 12 Nutrition + Measurements depth]
    ND --> PC[PR 13 Photo pipeline + Load calibration]
    PC --> AL[PR 14 Locale + Adaptive + Golden]
    AL --> B[PR 15 CI + Release hardening]
    B --> RP[PR 16 Signing + Performance]
    RP --> LG[PR 17 List-detail + Golden]
    LG --> LR[PR 18 Legacy photo recovery]
```

## Birleştirme durumu

GitHub PR #1–#11, 2026-07-16 tarihinde sırayla merge commit kullanılarak `master`a alındı. İlk on stacked PR için her adımda çocuk PR `master`a yeniden hedeflendi; yalnız kendi commit’lerinin kaldığı ve clean/mergeable olduğu doğrulandı. PR #11 post-merge CI düzeltmesini taşıdı. Son code baseline `3f2f8cf`; commit soy ağacı ve on bir ayrı first-parent merge commit korunur. Açık PR kalmadı ve kaynak dallar silinmedi. Otomatik master kabul koşusu `29499237460` tamamen yeşildir.

## PR 1 — Build hygiene

Node: [[ARCH_BUILD_DI]].
Çıktı: wrapper, README/JDK17, ProGuard, permission/temp cleanup, lint error 0.
Kabul: temiz clone `test lint assembleDebug` komut zincirine hazır.

## PR 2 — Test foundation

Node: [[TEST_MATRIX]].
Çıktı: JUnit/coroutine/Room/Compose test altyapısı, CI, ilk Clock/validator/DAO testleri.
Kabul: test görevi artık NO-SOURCE değildir.

## PR 3 — Database safety

Node: [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]].
Çıktı: schema export, migration sınıfları, destructive fallback kaldırma, fixture.
Kabul: desteklenen eski DB’lerden veri kayıpsız upgrade.

## PR 4 — Seed safety

Node: [[FEATURE_PROGRAMS]].
Çıktı: seedKey/contentVersion unique, transaction, ViewModel’den seed çıkarma.
Kabul: repeated/concurrent çağrıda tam 5 program; yarım yazma yok.

## PR 5 — Photo storage

Node: [[ADR_004_PERSISTENT_PHOTO_STORAGE]].
Durum: çekirdek kalıcılık tamamlandı.
Çıktı: picker import repository’si, app-owned file store, v5 metadata/migration, hata telafisi ve cleanup testleri.
Kabul: 9 cihaz testi geçiyor; EXIF/thumbnail, gerçek reboot ve backup politikası sonraki fotoğraf medya diliminde.

## PR 6 — Navigation + state skeleton

Node: [[ARCH_NAVIGATION]], [[ARCH_STATE_DATA_FLOW]], [[FEATURE_HOME]].
Durum: çekirdek iskelet tamamlandı.
Çıktı: 5 hedef shell, ID route, lifecycle state, reaktif Home dikey örneği.
Kabul: 8 JVM + 15 cihaz testi; detail/back/Activity recreation ve üç kaynaklı Home yenilemesi geçiyor. Harici deep link ve adaptive rail sonraki tasarım diliminde.

## PR 7 — Design system + Home

Node: [[DESIGN_SYSTEM_GRAPH]], [[FEATURE_HOME]].
Durum: temel ve Home referans uygulaması tamamlandı.
Çıktı: light/dark tema, type/spacing/shape/size tokenları, ortak metric/action/section/state bileşenleri, loading/content/error, midnight period tick, sahte sıfırsız empty state, font-aware compact/expanded Home.
Kabul: 11 JVM + 18 cihaz testi; lint 0 hata; light/dark/%200 font gerçek API 34 görsel kontrolü. Kalıcı golden altyapısı, navigation rail ve uygulama geneli bileşen dönüşümü sonraki dilimlerde.

## PR 8 — Workout lifecycle + modern kritik akış

Node: [[FEATURE_WORKOUT]], [[ADR_006_WORKOUT_SESSION_LIFECYCLE]], [[ARCH_DATABASE]].
Durum: uygulama ve kalite kapısı tamamlandı; PR8 yayın kapsamına alındı.
Çıktı: Room v6 status/completedAt migration, legacy completed politikası, tek aktif start/resume transaction’ı, status-korumalı mutation’lar, salt-okunur geçmiş, aktif Home CTA ve adaptive modern Workout ekranları.
Kabul: 11 JVM + 23 cihaz testi; lint 0 hata; v3→v6 migration; light/dark/%200 font gerçek API 34 kontrolü.

## PR 9 — Nutrition + Measurements doğruluk ve modernizasyon

Node: [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ARCH_STATE_DATA_FLOW]].
Durum: uygulama ve kalite kapısı tamamlandı; `341bf79` commit’i stacked `codex/nutrition-measurements` dalında draft PR #2 olarak yayınlandı ve PR8 dalını hedefliyor.
Çıktı: DST-safe `LocalDate + Clock + ZoneId`, SavedStateHandle tarih seçimi, zero-goal/over-goal güvenliği, locale ondalık parser, ölçüm aralıkları/latest-history/trend ayrımı, adaptive modern ekranlar ve kaynak metinleri.
Kabul: 17 JVM + 27 cihaz testi geçti; lint 0 hata; light/dark/%200 font gerçek API 34 kontrolü tamamlandı.

## PR 10 — Progress Photos + Muscle Load modernizasyonu

Node: [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]], [[ADR_005_MUSCLE_LOAD_NAMING]].
Durum: `2ad7ad8` commit’i `codex/progress-load` dalında stacked draft PR #3 olarak yayınlandı ve PR #2’yi hedefliyor.
Çıktı: device-local backup politikası, orphan/missing reconciliation, photo input validation/viewer/adaptive grid; completed-only reaktif load Flow, recency/bodyweight/unknown kuralları, metodoloji ve erişilebilir liste; modern Progress Hub.
Kabul: 20 JVM + 33 cihaz testi, lint 0 hata/67 uyarı, assemble ve light/dark/%200 API 34 görsel kontrolü.

## PR 11 — Programs normalizasyonu ve workout entegrasyonu

Node: [[FEATURE_PROGRAMS]], [[ADR_007_RELATIONAL_PROGRAM_PLANS]], [[ARCH_DATABASE]].
Durum: `7917358` commit’i `codex/programs-normalization` dalında stacked draft PR #4 olarak yayınlandı ve PR #3’ü hedefliyor.
Çıktı: Room v7 ilişkisel `ProgramDayExercise`, legacy JSON backfill/fallback, kararlı egzersiz ID, locked/ownership/target repository guard, atomic program→ACTIVE workout copy, planlanan-set completion/finish sözleşmesi ve modern adaptive Programs list/detail.
Kabul: 22 JVM + 41 cihaz testi; lint 0 hata/71 uyarı; assemble; light normal ve dark %200 Programs/detail/planned-workout API 34 görsel kontrolü.

## PR 12 — Nutrition/Measurements derinliği

Node: [[FEATURE_NUTRITION]], [[FEATURE_MEASUREMENTS]], [[ADR_008_SNAPSHOT_SAFE_EDIT_UNDO]], [[ARCH_DATABASE]].
Durum: `9c90471` commit’i `codex/nutrition-measurements-depth` dalında stacked draft PR #5 olarak yayınlandı ve PR #4’ü hedefliyor.
Çıktı: Room v8 measurement date index + period query/repository; 7/30/90/tümü gerçek kilo chart; özel tarih/edit/delete-undo; Nutrition snapshot-safe amount/meal edit + undo; scoped English resources, locale tarih, plural/birim ve dil-bağımsız testler.
Kabul: 24 JVM + 46 cihaz testi; lint 0 hata/77 uyarı; assemble; light normal ve dark %200 Nutrition/Measurements/chart API 34 görsel kontrolü.

## PR 13 — Photo media pipeline + personal load calibration

Node: [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]], [[ADR_005_MUSCLE_LOAD_NAMING]].
Durum: `5c3ce35` kod + `d189f6c` graph commit’leri `codex/photo-load-calibration` dalında stacked draft PR #6 olarak yayınlandı ve PR #5’i hedefliyor.
Çıktı: Room v9 photo thumbnail/dimension metadata; bounded decode, EXIF normalize, 2048 ana JPEG + 512 thumbnail ve iki dosyalı rollback/reconciliation; latest-valid measurement Flow’u ile kişisel bodyweight kalibrasyonu ve açık fallback UI.
Kabul: 26 JVM + 50 API 34 instrumentation testi; lint 0 hata/78 uyarı; assemble başarılı.

## PR 14 — App-wide locale + adaptive navigation + golden baseline

Node: [[ARCH_NAVIGATION]], [[ADR_002_FIVE_TARGET_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], [[TEST_MATRIX]].
Durum: `41c2ceb` kod + `f65e162` graph commit’leri `codex/adaptive-locale-goldens` dalında PR13’ü hedefleyen stacked draft PR #7 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/7
Çıktı: 350/350 TR/EN kaynak eşitliği; locale-aware kas/para gösterimi; compact bottom bar + 600 dp+ rail; Progress Hub birleşik semantics; English light ve Turkish dark/%200 kalıcı golden baseline.
Kabul: 28 JVM + 54 API 34 instrumentation testi; lint ve assemble başarılı.

## PR 15 — CI + minified release hardening

Node: [[ARCH_BUILD_DI]], [[TEST_MATRIX]], [[RISK_REGISTER_GRAPH]].
Durum: `abd3fbe` kod commit’i `codex/ci-release-hardening` dalında PR14’ü hedefleyen stacked draft PR #8 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/8
Çıktı: Linux-executable wrapper, tek `ciQuality`, PR/main quality job’u, API 34 device/golden job’u, 14 günlük rapor/release artifact’leri, R8 + resource shrinking ve APK/AAB/mapping üretimi.
Kabul: 28 JVM + 54 API 34 instrumentation testi; lint/debug; 2.56 MB minified APK, 4.81 MB AAB ve mapping başarılı. Hosted koşu `29459249716` quality 8 dk 51 sn ve API 34 device/golden 13 dk 13 sn ile tamamen yeşil.

## PR 16 — Secret-backed release + measured performance

Node: [[ARCH_BUILD_DI]], [[ADR_009_SECRET_BACKED_RELEASES]], [[PERFORMANCE_BASELINE]], [[TEST_MATRIX]].
Durum: `a2586d1` kod commit’i `codex/release-performance` dalında PR15’i hedefleyen stacked draft PR #9 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/9
Çıktı: protected production workflow, fail-fast dört secret, runner-temp keystore, APK v2/v3 + AAB JAR signature doğrulaması; generated Baseline/Startup Profile; startup/Home scroll/Workout macrobenchmark.
Kabul: `ciQuality`, 54/54 API 34 regresyon; 2/2 profil üretici; 4 macrobenchmark senaryosu; dummy keystore ile signed releaseDistribution yeşil. Gerçek secret/Play smoke ve fiziksel-device performans kabulü operasyonel takip.

## PR 17 — Workout/Programs list-detail + golden expansion

Node: [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], [[FEATURE_WORKOUT]], [[FEATURE_PROGRAMS]], [[TEST_MATRIX]].
Durum: `a573ab9` kod commit’i `codex/list-detail-goldens` dalında PR16’yı hedefleyen stacked draft PR #10 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/10
Çıktı: ortak 840 dp `GtListDetailScaffold`; compact ID-route davranışını koruyan Workout/Programs expanded seçim + özet panelleri; saved selection; selected semantics; English/light Programs ve Turkish/dark/%130 Workout kalıcı golden’ları.
Kabul: 30 JVM + 58 API 34 instrumentation testi; `ciQuality`, lint, debug ve minified release/profile kapısı başarılı; 356/356 TR/EN kaynak eşitliği. Hosted run `29474876318` iki job ile tamamen yeşil, artifact XML 58/58.

## PR 18 — Legacy photo recovery + optimization

Node: [[FEATURE_PROGRESS_PHOTOS]], [[ADR_004_PERSISTENT_PHOTO_STORAGE]], [[TEST_MATRIX]].
Durum: `e28a22d` kod commit’i `codex/legacy-photo-recovery` dalında hazır GitHub PR #12 olarak yayınlandı: https://github.com/MehmetKucukOfficial/GymTracker/pull/12
Çıktı: v0 dış sağlayıcı kayıtları için yeniden seçim; v1 app-owned kayıtlar için doğrudan v2 optimizasyon; aynı Room kimliği ve kullanıcı metadata’sı; update başarısızlığında iki yeni dosyanın rollback’i; başarılı swap sonrasında eski yönetilen dosya temizliği; ayrı erişilebilir kart/viewer eylemleri.
Kabul: 30 JVM + 62 API 34 instrumentation testi; hedefli photo paketi 13/13; `ciQuality`, lint, debug ve minified release/profile kapısı; light normal ve dark %200 yükseltme UI doğrulaması. Hosted run `29503247012`: quality 7m23s, API 34 device/golden 9m41s, artifact XML 62/62.

## Sonraki dikey dilim

1. Production secret + Play internal-track smoke ve fiziksel cihaz performans kabulü
2. Harici deep link + gerçek OS process-death kabul paketi
3. Kalan kritik ekran/state golden kapsamı ve ortak top-app-bar/feedback bileşenleri

Detaylı süre: [[GELISTIRME_MASTER_PLANI#18. Fazlar ve süre tahmini]]. Aktif durum: [[SESSION_HANDOFF]].
