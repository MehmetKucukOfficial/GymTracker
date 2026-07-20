---
type: feature
status: current
implementation_status: active
verified_commit: 4906402
updated: 2026-07-20
source_files:
  - app/src/main/java/com/gymtracker/data/settings/AppSettings.kt
  - app/src/main/java/com/gymtracker/di/SettingsModule.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsViewModel.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsScreen.kt
  - app/src/main/java/com/gymtracker/MainActivity.kt
  - app/src/main/java/com/gymtracker/ui/navigation/NavGraph.kt
  - app/src/main/java/com/gymtracker/ui/screens/home/HomeScreen.kt
  - app/src/test/java/com/gymtracker/data/settings/AppSettingsTest.kt
  - app/src/androidTest/java/com/gymtracker/ui/screens/settings/SettingsScreenTest.kt
  - app/src/androidTest/java/com/gymtracker/data/settings/SettingsRepositoryTest.kt
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

## Açık ürün dilimleri

- Birim sistemi ancak bütün kg/cm gösterim ve girişleri dönüşüm sözleşmesine bağlandığında kullanıcıya açılacak.
- Haftanın ilk günü ancak takvim/hafta sorgularını gerçekten etkilediğinde kullanıcıya açılacak.
- Varsayılan dinlenme süresi ve haptic tercihleri, workout rest-timer davranışıyla aynı dilimde eklenecek.
- Versiyonlanmış export/restore, güçlü onaylı tüm veriyi silme ve backup açıklaması ayrı veri yönetimi kabul kapısıdır.

## Kabul

- Geçen: 3 settings JVM testi; gerçek Preferences DataStore round-trip cihaz testi; hedefli 11 cihaz testi; 9-test Navigation smoke paketi.
- Geçen: explicit açık tema seçimi Activity recreation sonrasında seçili kalır.
- Geçen: Settings rotasında ana navigation gizlenir; top-bar geri Home’a döner.
- Geçen: tam `ciQuality`; 40/40 JVM, lint, debug, minified APK/AAB, R8 mapping ve embedded profile.
- Geçen: gerçek API 34 light/büyük-font görsel QA; top bar, üç tema seçeneği, gizlilik ve sürüm kartları kırpılmadan erişiliyor.
- Geçen: tam yerel `connectedDebugAndroidTest`, 80/80 app cihaz testi.
- Bekleyen: PR24 hosted API 34 kabulü, export/restore, tüm veriyi silme ve davranışa bağlı diğer tercihler.

Bağlar: [[ARCH_STATE_DATA_FLOW]], [[ARCH_NAVIGATION]], [[DESIGN_SYSTEM_GRAPH]], [[ROADMAP_EXECUTION_GRAPH]].
