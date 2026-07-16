---
type: feature
status: current
implementation_status: current
verified_commit: e28a22d
updated: 2026-07-16
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgressPhotoDao.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoSourceReader.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/data/photos/ProgressPhotoRepository.kt
  - app/src/main/java/com/gymtracker/data/photos/ProgressPhotoRules.kt
  - app/src/main/res/xml/backup_rules.xml
  - app/src/main/res/xml/data_extraction_rules.xml
  - app/src/main/AndroidManifest.xml
related_data: [ProgressPhoto, PhotoAngle]
tags: [gymtracker/feature, gymtracker/feature/photos, gymtracker/risk/p0]
---

# Feature — İlerleme Fotoğrafları

## Mevcut yetenek

`GetContent` ile görsel seçer; repository içeriği IO dispatcher üzerinde `filesDir/progress_photos` dizinine alır. Medya hattı 25 MB sınırından sonra gerçek decode doğrulaması yapar, EXIF yönünü uygular, ana görseli en fazla 2048 px ve grid thumbnail’ini en fazla 512 px olacak şekilde JPEG’e dönüştürür. İki çıktı kalıcı konuma atomik olarak yayınlanmadan DB satırı açılmaz. `ProgressPhoto` ana/thumbnail URI, normalize genişlik/yükseklik, MIME, byte boyutu ve storage sürümünü tutar. Grid thumbnail’i, viewer optimize ana görseli kullanır.

ViewModel açılışta DB ile yönetilen klasörü uzlaştırır: kaydı olmayan orphan/temp dosyaları siler; `storageVersion>=1` olup ana dosyası bulunmayan metadata satırlarını ve ilişkili thumbnail’i kaldırır. Yalnız thumbnail kayıpsa satırı korur ve thumbnail referansını temizleyerek ana görsele geri düşer. Legacy `storageVersion=0` satırlara dokunmaz ve UI risk rozeti gösterir.

PR18, aynı kayıt kimliğini ve kullanıcının tarih/açı/kilo/not metadata’sını koruyan yükseltme akışını ekler. v0 kayıt kullanıcıya sistem seçicisinden doğru fotoğrafı yeniden seçtirir; v1 kayıt mevcut app-owned dosyayı doğrudan v2 EXIF/resize/thumbnail hattından geçirir. Yeni dosyalar DB güncellemesinden önce hazırlanır, update başarısızsa birlikte geri alınır; eski yönetilen dosyalar yalnız kalıcı satır değişiminden sonra temizlenir.

## P0 risk durumu

Yeni importlar için geçici URI ve kontrolsüz büyük decode riski kapatıldı: PR13 kayıtları `storageVersion=2`, normalize app-owned ana/thumbnail `file://` URI’ları ve dimension metadata kullanır. v5/v8 kayıtları veri kaybetmeden v0/v1 olarak korunur. PR18 ile kırık v0 sağlayıcı kaydı yeniden seçimle, v1 app-owned kayıt ise tek eylemle v2’ye yükseltilebilir. Cihaz içi fotoğraflar privacy/quota gerekçesiyle cloud backup ve device transfer dışında bırakılmıştır.

## Diğer sorunlar

- Kamera ve medya izinleri kaldırıldı; sistem seçici akışı ek izin istemiyor.
- Import dialog URI’si gerçek OS process death sırasında korunmaz.
- Kalıcı screenshot-golden, gerçek düşük alan/reboot ve backup/restore cihaz kabulü bekler.

## Uygulanan karar

[[ADR_004_PERSISTENT_PHOTO_STORAGE]] kabul edildi. Room v9 `thumbnailUri/widthPx/heightPx`, storage v2 EXIF/resize/compress/thumbnail hattı, iki dosyalı rollback/reconciliation ve kimlik koruyan v0/v1 yükseltme sözleşmesi uygulanmıştır.

## Zorunlu testler

- Geçen: başarılı import/EXIF dönüşümü/2048 ana görsel/512 thumbnail/dimension metadata, storage yeniden oluşturma, iki dosyalı delete ve DB rollback, 25 MB ve bozuk decode temizliği, orphan/missing-main/missing-thumbnail reconciliation, v3→v9 ve v8→v9 satır koruma, v0 yeniden seçim, v1 yeniden optimizasyon, update rollback, kimlik/metadata koruma ve light/dark/%200 font yükseltme eylemleri.
- Bekleyen: gerçek process kill/restart, reboot, düşük alan, backup/restore cihaz kabulü ve geniş veri setinde thumbnail grid performans profili.

Bağlar: [[ARCH_DATABASE]], [[RISK_REGISTER_GRAPH#R2 — Fotoğraf URI kaybı]], [[SCREEN_BLUEPRINT_GRAPH#Fotoğraflar]].
