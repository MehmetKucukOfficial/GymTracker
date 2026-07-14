---
type: adr
adr: 4
status: accepted
implementation_status: partial
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/photos, gymtracker/risk/p0]
---

# ADR 004 — Kalıcı Fotoğraf Saklama

## Bağlam

Önceki akışta `GetContent` URI’si doğrudan DB’ye yazılıyordu; kalıcı izin yoktu. Provider/restart/restore sonrası görsel kırılabiliyordu.

## Karar

Varsayılan: sistem seçicisinden alınan içeriği background IO’da uygulamanın internal storage alanına kopyala. DB yalnızca başarılı kopyadan sonra `file://` URI, MIME, byte boyutu ve `storageVersion` kaydeder. Dış sağlayıcının geçici URI iznine çalışma zamanında bağımlı kalınmaz.

İlk uygulama dilimi kopyalama, 25 MB sınırı, geçici dosya temizliği, DB hata telafisi ve kontrollü silmeyi kapsar. EXIF orientation, yeniden boyutlandırma/sıkıştırma, dimension ve thumbnail üretimi sonraki fotoğraf medya hattına bırakılmıştır.

## Alternatif

`OpenDocument + takePersistableUriPermission`: depolama kopyası daha az, fakat provider izin/restore bağımlılığı sürer. Sadece kullanıcı “orijinali yerinde tut” deneyimi istiyorsa seçilmeli.

## Tutarlılık

- Import başarısızsa yarım dosya temizlenir.
- DB insert başarısızsa dosya temizlenir.
- Silme DB kaydını kaldırır ve yalnızca yönetilen `progress_photos` dizininin doğrudan çocuklarını fiziksel olarak temizler; dış `content://` kaynaklarına dokunmaz.
- Internal fotoğrafların Android backup kapsamı açık karardır.

## Kabul

- [x] Import edilen byte’lar internal storage’a kopyalanır ve yeniden oluşturulan storage nesnesinden erişilir.
- [x] Kaynak okuma hatasında DB satırı oluşmaz.
- [x] Silmede DB satırı ve yönetilen dosya temizlenir.
- [x] v3 legacy `content://` satırı v5 migration sırasında korunur ve `storageVersion=0` olarak işaretlenir.
- [ ] Gerçek process death/reboot, düşük alan, bozuk görsel decode ve backup/restore testleri.
- [ ] EXIF/resize/compress/thumbnail medya hattı.

Bağlar: [[FEATURE_PROGRESS_PHOTOS]], [[ARCH_DATABASE]], [[TEST_MATRIX]].
