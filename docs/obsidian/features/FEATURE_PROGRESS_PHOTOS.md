---
type: feature
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-14
source_files:
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressViewModel.kt
  - app/src/main/java/com/gymtracker/data/dao/ProgressPhotoDao.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoSourceReader.kt
  - app/src/main/java/com/gymtracker/data/photos/PhotoFileStorage.kt
  - app/src/main/java/com/gymtracker/data/photos/ProgressPhotoRepository.kt
  - app/src/main/AndroidManifest.xml
related_data: [ProgressPhoto, PhotoAngle]
tags: [gymtracker/feature, gymtracker/feature/photos, gymtracker/risk/p0]
---

# Feature — İlerleme Fotoğrafları

## Mevcut yetenek

`GetContent` ile görsel seçer; repository içeriği IO dispatcher üzerinde `filesDir/progress_photos` dizinine kopyalar. Kopya tamamlanmadan DB satırı açılmaz. `ProgressPhoto` artık yönetilen URI yanında MIME, byte boyutu ve storage sürümünü tutar. Ekran kayıt sırasında progress yüzeyi, sonuçta Snackbar gösterir; iki kolon grid ve açı filtresi korunur.

## P0 risk durumu

Yeni importlar için geçici URI riski kapatıldı: kayıtlar `storageVersion=1` ve app-owned `file://` URI kullanır. v5 migration eski satırları veri kaybetmeden `storageVersion=0` olarak tutar; bu legacy `content://` satırları sağlayıcı izni kaybolmuşsa hâlâ açılamayabilir. Otomatik legacy re-import mümkün değildir; kullanıcıdan yeniden seçim gerekir.

## Diğer sorunlar

- Kamera ve medya izinleri kaldırıldı; sistem seçici akışı ek izin istemiyor.
- Filtre chip satırı dar ekranda taşabilir.
- Loading/error placeholder ve full-screen viewer yok.
- Not UI’da görünmez.
- EXIF orientation, resize/compress, thumbnail ve dimension metadata henüz yok.
- Silmede dosya temizleme başarısız olursa görünmeyen orphan dosya kalabilir; periyodik orphan uzlaştırması henüz yok.
- Backup/restore kapsamı ve kullanıcıya açık veri taşıma politikası tanımlanmalı.

## Uygulanan karar

[[ADR_004_PERSISTENT_PHOTO_STORAGE]] kabul edildi. Kalıcı internal kopya, atomik geçici dosya yaklaşımı, 25 MB sınırı ve metadata v5’te uygulandı. Medya optimizasyon hattı ve thumbnail ikinci dilimdir.

## Zorunlu testler

- Geçen: başarılı import/kopya/metadata, storage yeniden oluşturma, delete cleanup, kaynak okuma hatasında rollback, v3→v5 legacy satır koruma.
- Bekleyen: gerçek process kill/restart, reboot, düşük alan, bozuk dosya decode, DB insert hata enjeksiyonu, backup/restore ve thumbnail grid performansı.

Bağlar: [[ARCH_DATABASE]], [[RISK_REGISTER_GRAPH#R2 — Fotoğraf URI kaybı]], [[SCREEN_BLUEPRINT_GRAPH#Fotoğraflar]].
