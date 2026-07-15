---
type: feature
status: current
implementation_status: current
verified_commit: 341bf79
updated: 2026-07-15
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

`GetContent` ile görsel seçer; repository içeriği IO dispatcher üzerinde `filesDir/progress_photos` dizinine kopyalar. Kopya tamamlanmadan DB satırı açılmaz. `ProgressPhoto` yönetilen URI yanında MIME, byte boyutu ve storage sürümünü tutar. Adaptive grid, yatay açı filtresi, boş/kayıt/hata state’leri, not/kilo metadata’sı ve büyük fotoğraf viewer uygulanmıştır.

ViewModel açılışta DB ile yönetilen klasörü uzlaştırır: kaydı olmayan orphan/temp dosyaları siler; `storageVersion=1` olup dosyası bulunmayan metadata satırlarını kaldırır. Legacy `storageVersion=0` satırlara dokunmaz ve UI risk rozeti gösterir.

## P0 risk durumu

Yeni importlar için geçici URI riski kapatıldı: kayıtlar `storageVersion=1` ve app-owned `file://` URI kullanır. v5 migration eski satırları veri kaybetmeden `storageVersion=0` olarak tutar; bu legacy `content://` satırları sağlayıcı izni kaybolmuşsa hâlâ açılamayabilir. Otomatik legacy re-import mümkün değildir; kullanıcıdan yeniden seçim gerekir. Cihaz içi fotoğraflar privacy/quota gerekçesiyle cloud backup ve device transfer dışında bırakılmış, restore sonrası metadata tutarlılığı reconciliation ile güvenceye alınmıştır.

## Diğer sorunlar

- Kamera ve medya izinleri kaldırıldı; sistem seçici akışı ek izin istemiyor.
- EXIF orientation, resize/compress, thumbnail ve dimension metadata henüz yok.
- Legacy yeniden seçim/iyileştirme akışı yok; yalnız açık risk etiketi vardır.
- Import dialog URI’si gerçek OS process death sırasında korunmaz.
- Kalıcı screenshot-golden ve gerçek düşük alan/reboot kabulü bekler.

## Uygulanan karar

[[ADR_004_PERSISTENT_PHOTO_STORAGE]] kabul edildi. Kalıcı internal kopya, atomik geçici dosya yaklaşımı, 25 MB sınırı, input validation, reconciliation ve cihaz-yerel backup politikası uygulandı. Medya optimizasyon hattı ve thumbnail sonraki dilimdir.

## Zorunlu testler

- Geçen: başarılı import/kopya/metadata, storage yeniden oluşturma, delete cleanup, kaynak/DB hatasında rollback, 25 MB temizliği, orphan/missing-row reconciliation, v3→v5 legacy satır koruma, legacy badge ve dark/%200 font empty state.
- Bekleyen: gerçek process kill/restart, reboot, düşük alan, bozuk dosya decode, backup/restore cihaz kabulü ve thumbnail grid performansı.

Bağlar: [[ARCH_DATABASE]], [[RISK_REGISTER_GRAPH#R2 — Fotoğraf URI kaybı]], [[SCREEN_BLUEPRINT_GRAPH#Fotoğraflar]].
