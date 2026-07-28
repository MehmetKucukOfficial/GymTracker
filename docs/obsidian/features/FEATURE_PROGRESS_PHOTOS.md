---
type: feature
status: current
implementation_status: current
verified_commit: 74b6d82
updated: 2026-07-28
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgressPhotoDao.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoSourceReader.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/data/photos/ProgressPhotoRepository.kt
  - app/src/main/java/com/gymtracker/data/photos/ProgressPhotoRules.kt
  - app/src/androidTest/java/com/gymtracker/ui/screens/progress/ProgressPhotoScreenTest.kt
  - app/src/androidTest/java/com/gymtracker/ui/golden/StateCoverageGoldenTest.kt
  - app/src/androidTest/java/com/gymtracker/data/portability/AndroidBackupTransportAcceptanceTest.kt
  - .github/scripts/android-backup-transport-acceptance.sh
  - app/src/main/res/xml/backup_rules.xml
  - app/src/main/res/xml/data_extraction_rules.xml
  - app/src/main/AndroidManifest.xml
related_data: [ProgressPhoto, PhotoAngle]
tags: [gymtracker/feature, gymtracker/feature/photos, gymtracker/risk/p0]
---

# Feature — İlerleme Fotoğrafları

## Mevcut yetenek

`GetContent` ile görsel seçer; repository içeriği IO dispatcher üzerinde `filesDir/progress_photos` dizinine alır. Medya hattı 25 MB sınırından sonra gerçek decode doğrulaması yapar, EXIF yönünü uygular, ana görseli en fazla 2048 px ve grid thumbnail’ini en fazla 512 px olacak şekilde JPEG’e dönüştürür. İki çıktı kalıcı konuma atomik olarak yayınlanmadan DB satırı açılmaz. `ProgressPhoto` ana/thumbnail URI, normalize genişlik/yükseklik, MIME, byte boyutu ve storage sürümünü tutar. Grid thumbnail’i, viewer optimize ana görseli kullanır.

PR23 import ve legacy optimizasyonundan önce gerçek dosya sistemindeki kullanılabilir alanı ölçer. 25 MB kaynak sınırı ile source/main/thumbnail geçici dosyalarını güvenli taşıyabilmek için 48 MB rezerv yoksa hiçbir temp dosya veya DB satırı oluşturmadan typed `InsufficientPhotoStorageException` döner; ViewModel bunu yerelleştirilmiş düşük alan mesajına çevirir.

ViewModel açılışta DB ile yönetilen klasörü uzlaştırır: kaydı olmayan orphan/temp dosyaları siler; `storageVersion>=1` olup ana dosyası bulunmayan metadata satırlarını ve ilişkili thumbnail’i kaldırır. Yalnız thumbnail kayıpsa satırı korur ve thumbnail referansını temizleyerek ana görsele geri düşer. Legacy `storageVersion=0` satırlara dokunmaz ve UI risk rozeti gösterir.

PR18, aynı kayıt kimliğini ve kullanıcının tarih/açı/kilo/not metadata’sını koruyan yükseltme akışını ekler. v0 kayıt kullanıcıya sistem seçicisinden doğru fotoğrafı yeniden seçtirir; v1 kayıt mevcut app-owned dosyayı doğrudan v2 EXIF/resize/thumbnail hattından geçirir. Yeni dosyalar DB güncellemesinden önce hazırlanır, update başarısızsa birlikte geri alınır; eski yönetilen dosyalar yalnız kalıcı satır değişiminden sonra temizlenir.

PR27, kayıt/optimizasyon sürerken yeni import, silme ve yükseltme mutation’larını kullanıcı ve semantics katmanında kilitler. Turkish dark/%200 kayıt durumu kalıcı pixel-golden ile korunur; animasyon saati sabitlenerek yükleme göstergesi deterministik karşılaştırılır.

## P0 risk durumu

Yeni importlar için geçici URI, kontrolsüz büyük decode ve alan tükenirken yarım yazma riski kapatıldı: PR13 kayıtları `storageVersion=2`, normalize app-owned ana/thumbnail `file://` URI’ları ve dimension metadata kullanır. v5/v8 kayıtları veri kaybetmeden v0/v1 olarak korunur. PR18 ile kırık v0 sağlayıcı kaydı yeniden seçimle, v1 app-owned kayıt ise tek eylemle v2’ye yükseltilebilir. Cihaz içi fotoğraflar privacy/quota gerekçesiyle cloud backup ve device transfer dışında bırakılmıştır; XML politika testleri cloud backup ile device transfer kapsamını ayrı ayrı doğrular. Gerçek host reboot kabulü 120 yönetilen ana/thumbnail çiftinin ve Room metadata’sının boot sayacı değiştikten sonra tutarlı kaldığını kanıtlar. PR29 gerçek Android local transport turu Room verisi ile koyu temayı yedekleyip `pm clear` sonrasında geri getirir; dışlanan ana/thumbnail byte’larının dönmediğini ve eksik managed metadata satırının reconciliation ile kaldırıldığını kanıtlar.

## Diğer sorunlar

- Kamera ve medya izinleri kaldırıldı; sistem seçici akışı ek izin istemiyor.
- Import dialog URI’si gerçek OS process death sırasında korunmaz.
- Android local backup transport restore kabulü otomatiktir. Üretim cloud sağlayıcısı/cihazdan-cihaza transport çeşitliliği platform dağıtım smoke’unda izlenir.
- Emülatör büyük-grid metriği yalnız ön-baseline’dır; fiziksel release cihaz kabulü [[PERFORMANCE_BASELINE]] içinde açıktır.

## Uygulanan karar

[[ADR_004_PERSISTENT_PHOTO_STORAGE]] kabul edildi. Room v9 `thumbnailUri/widthPx/heightPx`, storage v2 EXIF/resize/compress/thumbnail hattı, iki dosyalı rollback/reconciliation ve kimlik koruyan v0/v1 yükseltme sözleşmesi uygulanmıştır.

## Zorunlu testler

- Geçen: başarılı import/EXIF dönüşümü/2048 ana görsel/512 thumbnail/dimension metadata, storage yeniden oluşturma, iki dosyalı delete ve DB rollback, 25 MB/48 MB rezerv/bozuk decode temizliği, orphan/missing-main/missing-thumbnail ve metadata-only restore reconciliation, v3→v9 ve v8→v9 satır koruma, v0 yeniden seçim, v1 yeniden optimizasyon, update rollback, kimlik/metadata koruma, light/dark/%200 font yükseltme eylemleri, kayıt sırasında çakışan mutation kilidi, Turkish dark/%200 kalıcı saving golden, 500 satırlı lazy-grid erişimi, gerçek host reboot sonrası 120 dosya çifti ve 120-row FrameTiming macrobenchmark.
- Geçen: gerçek Android local backup transport seed/backup/`pm clear`/restore/reconciliation kabulü; hosted run `30389433873` içinde package backup success/310.784 byte, token `1`, `restoreFinished: 0` ve verify 1/1.
- Bekleyen: fiziksel cihaz performans kabulü.

Bağlar: [[ARCH_DATABASE]], [[RISK_REGISTER_GRAPH#R2 — Fotoğraf URI kaybı]], [[SCREEN_BLUEPRINT_GRAPH#Fotoğraflar]].
