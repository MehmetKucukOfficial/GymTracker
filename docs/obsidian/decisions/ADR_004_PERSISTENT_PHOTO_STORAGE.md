---
type: adr
adr: 4
status: accepted
implementation_status: current
date: 2026-07-14
verified_commit: 729ecc6
updated: 2026-07-28
tags: [gymtracker/decision, gymtracker/photos, gymtracker/risk/p0]
---

# ADR 004 — Kalıcı Fotoğraf Saklama

## Bağlam

Önceki akışta `GetContent` URI’si doğrudan DB’ye yazılıyordu; kalıcı izin yoktu. Provider/restart/restore sonrası görsel kırılabiliyordu.

## Karar

Varsayılan: sistem seçicisinden alınan içeriği background IO’da uygulamanın internal storage alanına kopyala. DB yalnızca başarılı kopyadan sonra `file://` URI, MIME, byte boyutu ve `storageVersion` kaydeder. Dış sağlayıcının geçici URI iznine çalışma zamanında bağımlı kalınmaz.

İlk uygulama dilimi kopyalama, 25 MB sınırı, geçici dosya temizliği, DB hata telafisi ve kontrollü silmeyi kapsar. PR13 medya hattı kaynak byte’ları geçici dosyaya sınırla kopyalar; gerçek decode doğrulaması ve EXIF orientation uygular; uzun kenarı en çok 2048 px ana JPEG ve en çok 512 px grid thumbnail’i üretir. Room v9 ana/thumbnail URI, normalize genişlik/yükseklik ve storage v2 sözleşmesini taşır.

PR10 kararı: hassas ve yüksek boyutlu `files/progress_photos` içeriği cloud backup ve device transfer dışında kalır. Room metadata restore edilip dosya yoksa açılış reconciliation’ı yönetilen eksik satırı kaldırır. DB’de karşılığı olmayan managed orphan/temp dosyalar da temizlenir; legacy `storageVersion=0` dış URI’lara dokunulmaz.

PR18 kararı: v0 dış URI kaydı yeniden seçimle, v1 app-owned kayıt doğrudan yeniden optimizasyonla v2’ye taşınır. Yükseltme yeni ana/thumbnail dosyalarını önce üretir, aynı Room kimliğini ve kullanıcı metadata’sını koruyarak satırı değiştirir, eski yönetilen dosyayı yalnız başarılı update sonrasında temizler. Update sıfır satır etkiler veya hata verirse yeni dosyalar rollback edilir.

PR23 kararı: import veya optimizasyon başlamadan önce app-owned dizinin kullanılabilir alanı ölçülür. 25 MB kaynak üst sınırı, eşzamanlı source/main/thumbnail geçici çıktıları ve atomik publish payı için 48 MB rezerv zorunludur. Yetersiz alan typed hata olarak UI’a taşınır; temp dosya ve Room satırı yaratılmaz. Backup sözleşmesi “metadata restore olabilir, hassas medya dosyası restore/transfer edilmez” şeklindedir; açılış reconciliation’ı bu durumda eksik managed satırları temizler.

## Alternatif

`OpenDocument + takePersistableUriPermission`: depolama kopyası daha az, fakat provider izin/restore bağımlılığı sürer. Sadece kullanıcı “orijinali yerinde tut” deneyimi istiyorsa seçilmeli.

## Tutarlılık

- Import başarısızsa yarım dosya temizlenir.
- DB insert başarısızsa ana görsel ve thumbnail birlikte temizlenir.
- Silme DB kaydını kaldırır ve yalnızca yönetilen `progress_photos` dizininin doğrudan çocukları olan ana/thumbnail dosyalarını temizler; dış `content://` kaynaklarına dokunmaz.
- Ana görsel kayıpsa satır ve thumbnail kaldırılır; yalnız thumbnail kayıpsa satır korunur ve ana görsele geri düşülür.
- Legacy yükseltmede DB satırı yeni dosyalara geçmeden eski kaynak silinmez; başarısız update yeni dosyaları bırakmaz.
- Internal fotoğrafların Android backup kapsamı açık karardır: `backup_rules.xml` ile legacy cloud backup, `data_extraction_rules.xml` ile cloud backup ve device transfer dışındadır.
- CI host’u 120 yönetilen kayıt üretir, Android’i gerçekten reboot eder, `BOOT_COUNT` artışını ve bütün ana/thumbnail dosyaları ile metadata tutarlılığını doğrular; ardından aynı veri setinde thumbnail grid macrobenchmark çalıştırır.
- Android local backup transport kabulü üretim APK’sını gerçek `BackupManager` ile yedekler, uygulama verisini temizler ve restore-set token’ından geri yükler. Room/tema geri gelir; `progress_photos/` byte’ları gelmez ve açılış reconciliation sözleşmesi eksik managed metadata’yı kaldırır.

## Kabul

- [x] Import edilen byte’lar internal storage’a kopyalanır ve yeniden oluşturulan storage nesnesinden erişilir.
- [x] Kaynak okuma hatasında DB satırı oluşmaz.
- [x] Silmede DB satırı ve yönetilen dosya temizlenir.
- [x] v3 legacy `content://` satırı v5 migration sırasında korunur ve `storageVersion=0` olarak işaretlenir.
- [x] Orphan managed dosya ve eksik managed metadata açılışta uzlaştırılır.
- [x] Progress fotoğraf dosyaları backup/device transfer dışında bırakılır ve politika UI’da açıklanır.
- [x] EXIF orientation normalize edilir; bounded ana görsel + thumbnail ve dimension metadata üretilir.
- [x] Bozuk decode, iki dosyalı DB rollback ve missing-thumbnail onarımı testlidir.
- [x] Gerçek process death, host reboot, düşük alan ve metadata-only restore/reconciliation testleri.
- [x] Gerçek Android local backup transport restore ve eksik-medya reconciliation deneyi.
- [ ] Fiziksel cihaz performans kabulü.
- [x] Legacy v0 yeniden seçim ve v1 isteğe bağlı yeniden optimizasyon UX’i.

Bağlar: [[FEATURE_PROGRESS_PHOTOS]], [[ARCH_DATABASE]], [[TEST_MATRIX]].
