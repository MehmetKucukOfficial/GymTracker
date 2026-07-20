---
type: adr
status: accepted
implementation_status: export-current
verified_commit: 81cb7fe
updated: 2026-07-20
source_files:
  - app/src/main/java/com/gymtracker/data/portability/DataPortabilityDao.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchive.kt
  - app/src/main/java/com/gymtracker/data/portability/PortableArchiveWriter.kt
  - app/src/main/java/com/gymtracker/data/portability/DataExportRepository.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsViewModel.kt
  - app/src/main/java/com/gymtracker/ui/screens/settings/SettingsScreen.kt
  - app/src/test/java/com/gymtracker/data/portability/PortableArchiveWriterTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataPortabilityDaoTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/DataExportRepositoryTest.kt
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
8. Geri yükleme ayrı bir değişiklik dilimidir. Önce arşiv sürümü, JSON, enum/değerler, ilişkiler, medya hash/uzunlukları ve yeterli alan staging alanda doğrulanacak; sonra tek kontrollü replace transaction ve dosya rollback'i uygulanacaktır. Canlı veritabanına parça parça import yapılmayacaktır.
9. “Tüm veriyi sil” geri yüklemeden ayrı, güçlü onaylı ve dosya + Room + DataStore temizliğini birlikte ele alan bir dilimdir.

## Sonuçlar

- Manuel dışa aktarma bugün kullanılabilir ve testlidir; geri yükleme henüz uygulanmış sayılmaz.
- Format v1 daha sonra schema sürümünden bağımsız migrator zinciriyle okunabilir.
- Legacy provider fotoğraflarının byte'ları garanti edilmez; kullanıcı başarı özetinde metadata-only sayısını görür.
- Arşiv sağlık ve fotoğraf verisi içerebilir. UI hedef dosyanın güvenli saklanması gerektiğini açıkça belirtir.

## Kanıt

- `PortableArchiveWriterTest`: sürüm alanları, tüm typed veri dönüşümü, yönetilen ana/thumbnail byte'ları, legacy metadata-only politikası, cihaz yolu sızıntısı olmaması ve eksik medya fail-closed davranışı.
- `DataPortabilityDaoTest`: on bir tablonun tek transaction içinde kararlı sırayla okunması.
- `DataExportRepositoryTest`: gerçek seçili dosyaya okunabilir ZIP kopyası, tema/veri içeriği, geçici dosya temizliği ve düşük alanda hedefin bozulmaması.
- Hedefli API 34 paketi 19/19; JVM paketi 42/42; tam `ciQuality` başarılı.

Bağlar: [[FEATURE_SETTINGS_DATA]], [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]], [[CHANGE_IMPACT_MAP]], [[TEST_MATRIX]].
