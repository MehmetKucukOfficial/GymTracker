---
type: feature
status: current
implementation_status: active
verified_commit: 7e9d4c0
updated: 2026-07-28
source_files:
  - app/src/main/java/com/gymtracker/data/settings/AppSettings.kt
  - app/src/main/java/com/gymtracker/data/portability/DataPortabilityDao.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchive.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveWriter.kt
  - app/src/main/java/com/gymtracker/data/portability/DataExportRepository.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveReader.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveValidator.kt
  - app/src/main/java/com/gymtracker/data/portability/DataRestoreRepository.kt
  - app/src/main/java/com/gymtracker/data/portability/DataDeletionRepository.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/di/SettingsModule.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsViewModel.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsScreen.kt
  - app/src/main/java/com/gymtracker/MainActivity.kt
  - app/src/main/java/com/gymtracker/ui/navigation/NavGraph.kt
  - app/src/main/java/com/gymtracker/ui/screens/home/HomeScreen.kt
  - app/src/test/java/com/gymtracker/data/settings/AppSettingsTest.kt
  - app/src/androidTest/java/com/gymtracker/ui/screens/settings/SettingsScreenTest.kt
  - app/src/androidTest/java/com/gymtracker/ui/golden/StateCoverageGoldenTest.kt
  - app/src/androidTest/java/com/gymtracker/data/settings/SettingsRepositoryTest.kt
  - app/src/test/java/com/gymtracker/data/portability/PortableArchiveWriterTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataPortabilityDaoTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataExportRepositoryTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataRestoreRepositoryTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataDeletionRepositoryTest.kt
  - app/src/androidTest/java/com/gymtracker/ui/navigation/NavigationSmokeTest.kt
tags: [gymtracker/feature, gymtracker/settings, gymtracker/data]
---

# Özellik — Ayarlar ve Veri Yönetimi

## Uygulanan çekirdek

- Home başlığındaki 48 dp, yerelleştirilmiş ayarlar eylemi `settings` child rotasını açar.
- Ayarlar tam ekran child destination’dır; ana bottom bar/rail gizlenir ve ortak `GtTopAppBar` geri eylemi Home’a döner.
- Singleton Preferences DataStore, typed `SettingsRepository` ve `SettingsViewModel` üzerinden `SYSTEM/LIGHT/DARK` tercihini saklar.
- Activity-scoped settings state’i `GymTrackerTheme` seçimini anında değiştirir; explicit tema Activity recreation ve sonraki uygulama açılışında korunur.
- Bilgi kartı local-first saklama ve progress fotoğraflarının cloud/device-transfer dışında tutulduğunu açıklar; sürüm paket yöneticisinden okunur.
- Route/Content ayrımı, lifecycle-aware state collection, tek seçim semantics’i, TR/EN eş kaynak ve `%200` fontta kaydırılabilir düzen aktiftir.
- Manuel veri dışa aktarma SAF `CreateDocument` ile kullanıcı seçtiği ZIP hedefini kullanır; gizli bulut aktarımı veya geniş storage izni yoktur.
- Format v1 arşivi on bir Room tablosunu tek transaction anında, tema tercihini ve storage v1/v2 yönetilen fotoğraf byte'larını taşır. Cihaz `file://` yolları JSON'a girmez; provider-backed v0 fotoğraflar açıkça metadata-only işaretlenir.
- Cache'te tamamla-sonra-kopyala akışı, düşük alan preflight'i, eksik yönetilen medya hatası, bounded medya kopyası, canonical uygulama kökü ve geçici dosya temizliği aktiftir. Karar: [[ADR_010_VERSIONED_MANUAL_DATA_PORTABILITY]].
- Restore, yalnız desteklenen format v1/schema v9 arşivini kabul eder; JSON/enum/değer/ID/foreign-key/medya-path sözleşmesini fail-closed doğrular, giriş sayısı ile tekil/toplam açılmış boyutu sınırlar ve medya uzunluğu + ZIP CRC bütünlüğünü staging alanında denetler.
- Doğrulanan yönetilen medya yeni benzersiz app-owned dosyalara yayınlanır. On bir tablo tek Room replace transaction'ında değiştirilir; tema yazımı başarısız olursa eski DB/tema ve yeni dosyalar telafi edici rollback ile geri alınır. Başarıdan sonra eski yönetilen dosyalar temizlenir.
- Export, restore ve silme aynı uygulama-geneli mutex'i kullanır. Önceki süreç kapanmasından kalan yalnız ayrılmış export/restore cache çocukları yeni işlem başında temizlenir.
- “Tüm veriyi sil” ayrı hata/başarı state'i ve yerelleştirilmiş kesin onay sözcüğü gerektirir. Kullanıcı tabloları tek transaction'da temizlenir, paketle gelen beş program varsayılana seed edilir, tema `SYSTEM` olur ve yönetilen/artık fotoğraf dosyaları silinir.

## Açık ürün dilimleri

- Birim sistemi ancak bütün kg/cm gösterim ve girişleri dönüşüm sözleşmesine bağlandığında kullanıcıya açılacak.
- Haftanın ilk günü ancak takvim/hafta sorgularını gerçekten etkilediğinde kullanıcıya açılacak.
- Varsayılan dinlenme süresi ve haptic tercihleri, workout rest-timer davranışıyla aynı dilimde eklenecek.
- Gelecek format/schema sürümleri için açık archive migrator zinciri, gerçek süreç-kesilmesi kabul fixture'ı ve geri yükleme ilerleme yüzdesi sonraki dayanıklılık dilimleridir.

## Kabul

- Geçen: 3 settings JVM testi; gerçek Preferences DataStore round-trip cihaz testi; hedefli 11 cihaz testi; 9-test Navigation smoke paketi.
- Geçen: explicit açık tema seçimi Activity recreation sonrasında seçili kalır.
- Geçen: Settings rotasında ana navigation gizlenir; top-bar geri Home’a döner.
- Geçen: tam `ciQuality`; 40/40 JVM, lint, debug, minified APK/AAB, R8 mapping ve embedded profile.
- Geçen: gerçek API 34 light/büyük-font görsel QA; top bar, üç tema seçeneği, gizlilik ve sürüm kartları kırpılmadan erişiliyor.
- Geçen: tam yerel `connectedDebugAndroidTest`, 80/80 app cihaz testi.
- Geçen: PR24 hosted run `29775858318`; 40 JVM, 80 app, 4 process-death, 1 beş-iterasyon benchmark, reboot seed/verify 1+1; merge commit `80c79b5`.
- Geçen: PR25 dışa aktarma hedefli API 34 paketi 19/19, JVM 42/42 ve tam `ciQuality`; gerçek seçili dosyaya okunabilir ZIP, düşük alan, temp cleanup, path güvenliği, TR/EN ve `%200` font kanıtları.
- Geçen: temiz reboot sonrası hedefli Progress Hub golden 2/2 ve tam API 34 regresyon paketi 85/85, 0 failure/error/skipped.
- Geçen: PR26 hedefli restore/delete/DAO/Settings/localization paketi 16/16; gerçek export→mutate→restore, yönetilen ana/thumbnail yeniden yayınlama, bozuk arşivde mevcut veriyi koruma, FK hatasında Room rollback, güçlü onay sözcüğü ve tüm-veri temizliği kanıtları.
- Geçen: PR26 tam API 34 regresyon paketi 95/95, 0 failure/error/skipped; altı kalıcı golden baseline dahil.
- Geçen: PR26 hosted run `29783838425`; 95 app + 5 benchmark/process, reboot 1+1, dört process-death ve beş Perfetto trace, 0 failure/error/skipped; merge commit `a370f82`.
- Geçen: PR27 English/light restore-lock golden’ı; restore sırasında export/restore/delete eylemlerinin disabled semantics ve piksel görünümü.
- Bekleyen: gerçek süreç-kesilmesi fixture'ı ve davranışa bağlı diğer tercihler.

Bağlar: [[ARCH_STATE_DATA_FLOW]], [[ARCH_DATABASE]], [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]].
