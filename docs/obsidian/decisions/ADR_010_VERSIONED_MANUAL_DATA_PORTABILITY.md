---
type: adr
status: accepted
implementation_status: current
verified_commit: 729ecc6
updated: 2026-07-28
source_files:
  - app/src/main/java/com/gymtracker/data/portability/DataPortabilityDao.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchive.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveWriter.kt
  - app/src/main/java/com/gymtracker/data/portability/DataExportRepository.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveReader.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveValidator.kt
  - app/src/main/java/com/gymtracker/data/portability/DataRestoreRepository.kt
  - app/src/main/java/com/gymtracker/data/portability/DataPortabilityEntryPoint.kt
  - app/src/main/java/com/gymtracker/data/portability/DataDeletionRepository.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsViewModel.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsScreen.kt
  - app/src/test/java/com/gymtracker/data/portability/PortableArchiveWriterTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataPortabilityDaoTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataExportRepositoryTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataRestoreRepositoryTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/RestoreProcessKillAcceptanceTest.kt
  - .github/scripts/restore-process-kill-acceptance.sh
  - app/src/androidTest/java/com/gymtracker/data/portability/DataDeletionRepositoryTest.kt
tags: [gymtracker/decision, gymtracker/data, gymtracker/portability, gymtracker/privacy]
---

# ADR 010 — Versioned manual data portability

## Bağlam

GymTracker local-first çalışır. Room verisi ile uygulama tarafından yönetilen ilerleme fotoğrafları Android cloud backup kapsamının dışındadır. Kullanıcıya taşınabilir bir yedek sunmak; tutarlı bir veritabanı anı, hassas medya, sürüm uyumluluğu, boş alan, yarım dosya ve gelecekte güvenli geri yükleme konularını tek sözleşmede çözmeyi gerektirir.

## Karar

1. Kullanıcı `Settings` içinden Storage Access Framework `CreateDocument` akışıyla açıkça bir ZIP hedefi seçer. Arka planda gizli veya otomatik bulut aktarımı yapılmaz.
2. Arşiv kökünde `data.json` bulunur. `format=gymtracker-export`, `formatVersion=1` ve `databaseSchemaVersion=9` alanları okuyucunun fail-closed uyumluluk kontrolüdür.
3. On bir Room tablosu `DataPortabilityDao` ile ID sıralı ve tek `withTransaction` anında okunur. Bu salt-okunur DAO yeni entity veya şema değişikliği yaratmaz.
4. Tema tercihi typed ayar olarak arşive eklenir. Cihaza özgü `file://` yolları JSON'a yazılmaz.
5. Storage v1/v2 uygulama-yönetimli fotoğraflar güvenli `media/photos/{id}/...` girdileriyle arşive katılır. Storage v0 provider fotoğrafları izin sürekliliği garanti edilemediği için açıkça `LEGACY_METADATA_ONLY` işaretlenir.
6. Arşiv önce uygulama cache alanında tamamlanır ve doğrulanır; daha sonra seçilen hedefe kopyalanır. Düşük geçici alan, eksik yönetilen medya veya kopyalama hatası typed başarısızlık üretir. Geçici dosya her durumda temizlenir; hedef kopya başarısızsa silme best-effort uygulanır.
7. Medya okuma yalnızca canonical `progress_photos` kökü içindeki dosyalara izin verir. Tek medya girdisi 30 MB ile sınırlandırılır ve ZIP girdileri absolute/traversal path kabul etmez.
8. Geri yükleme arşiv sürümü/schema, strict JSON, enum/değer, pozitif/benzersiz ID, ilişkiler ve güvenli medya yollarını canlı veriye dokunmadan doğrular. Giriş sayısı, JSON, tekil/toplam açılmış medya ve çalışma alanı sınırlıdır; her medya staging sonrasında merkezi ZIP uzunluğu + CRC değeriyle karşılaştırılır.
9. Yönetilen medya yeni benzersiz dosyalara yayınlandıktan sonra on bir tablo tek `replaceAll` Room transaction'ında değiştirilir. Tema yazımı başarısız olursa eski DB/tema geri yazılır ve yeni dosyalar silinir; rollback de başarısızsa ayrı kritik hata üretilir. Eski medya yalnız kalıcı başarıdan sonra temizlenir.
10. Export, restore ve silme tek uygulama-geneli mutex altında seri çalışır. Ayrılmış cache dizinindeki süreç-kesilmesi artıkları bir sonraki işlem başında temizlenir; canlı DB'ye parça parça import yapılmaz.
11. “Tüm veriyi sil” geri yüklemeden ayrı ve yerelleştirilmiş kesin sözcük yazmayı gerektiren güçlü onaylı işlemdir. Kullanıcı verisi temizlenir, yerleşik program kataloğu aynı Room transaction'ında varsayılana döner, tema `SYSTEM` yapılır ve yönetilen/artık medya temizlenir. DB/ayar başarısızlığında eski snapshot telafi edici rollback ile geri yüklenir.
12. Host-orchestrated kabul fixture’ı restore’u medya yayınından sonra fakat Room/tema commit’inden önce durdurur. Host hedef PID’yi `am force-stop` ile öldürür; yeni süreç eski snapshot/tema/mevcut medya korunmasını, yarım cache temizliğini ve yayınlanmış orphan medyanın reconciliation ile kaldırılmasını kanıtlar.

## Sonuçlar

- Manuel dışa aktarma, staged geri yükleme ve güçlü onaylı veri silme uygulanmış ve hedefli testlerle kanıtlanmıştır.
- Format v1 daha sonra schema sürümünden bağımsız migrator zinciriyle okunabilir.
- Legacy provider fotoğraflarının byte'ları garanti edilmez; kullanıcı başarı özetinde metadata-only sayısını görür.
- Arşiv sağlık ve fotoğraf verisi içerebilir. UI hedef dosyanın güvenli saklanması gerektiğini açıkça belirtir.

## Kanıt

- `PortableArchiveWriterTest`: sürüm alanları, tüm typed veri dönüşümü, yönetilen ana/thumbnail byte'ları, legacy metadata-only politikası, cihaz yolu sızıntısı olmaması ve eksik medya fail-closed davranışı.
- `DataPortabilityDaoTest`: on bir tablonun tek transaction içinde kararlı sırayla okunması.
- `DataExportRepositoryTest`: gerçek seçili dosyaya okunabilir ZIP kopyası, tema/veri içeriği, geçici dosya temizliği ve düşük alanda hedefin bozulmaması.
- `PortableArchiveReaderTest`: strict manifest, güvenli path, ilişki doğrulama, medya çıkarma/CRC ve managed/legacy DB eşlemesi.
- `DataRestoreRepositoryTest`: gerçek export→mutate→restore, bozuk ZIP'te değişiklik yapmama, yeni yönetilen dosyalara yayın ve eski dosya temizliği.
- `RestoreProcessKillAcceptanceTest` + `restore-process-kill-acceptance.sh`: seed 1/1, gerçek pre-commit PID kill ve yeni süreç verify 1/1; eski veri/tema/medya korunması, iki orphan dosya ve yarım cache temizliği.
- `DataDeletionRepositoryTest`: kullanıcı tabloları/tema/yönetilen medya temizliği ve varsayılan program kataloğunun tutarlı yeniden kurulması.
- `DataPortabilityDaoTest`: geçersiz incoming FK yazımında transaction'ın eski veriyi geri getirmesi.
- PR25 yerel kabulü 42 JVM + 85 cihaz; PR26 hedefli restore/delete/DAO/Settings/localization paketi 16/16. PR26 tam ve hosted kapıları kayda geçmeden nihai kabul iddiası yapılmaz.

Bağlar: [[FEATURE_SETTINGS_DATA]], [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]], [[CHANGE_IMPACT_MAP]], [[TEST_MATRIX]].
