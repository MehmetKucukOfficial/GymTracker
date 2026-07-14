---
type: adr
adr: 6
status: accepted
implementation_status: current
date: 2026-07-15
tags: [gymtracker/decision, gymtracker/workout, gymtracker/data]
---

# ADR 006 — Antrenman Seansı Yaşam Döngüsü

## Bağlam

Önceki şemada antrenman seansının aktif mi tamamlanmış mı olduğu bilinmiyordu. Bu nedenle geçmiş kayıtlar aktif ekranında açılıyor, yeni seans başlatma birden fazla yarım kayıt üretebiliyor ve Home CTA’sı devam edilecek seansı ayırt edemiyordu.

## Karar

`WorkoutSession` iki kalıcı duruma sahiptir:

- `ACTIVE`: yalnızca set ekleme/silme ve bitirme işlemlerini kabul eder.
- `COMPLETED`: `completedAt` ve son süre/notları taşır; detay ekranında salt okunurdur.

Uygulama aynı anda en fazla bir aktif seansı kullanıcı akışı olarak destekler. `startOrResumeSession` Room transaction içinde mevcut aktif seansı döndürür; yoksa yenisini oluşturur. Set mutation’ları ve bitirme sorgusu status koşulunu SQL seviyesinde kontrol eder. UI kontrolü tek güvenlik sınırı değildir.

## Migration kararı

Room v6, `status` ve nullable `completedAt` kolonlarını ve status indeksini ekler. v5 ve daha eski desteklenen fixture’lardaki bütün legacy seanslar güvenli tarafta kalmak için `COMPLETED` kabul edilir. `completedAt`, `date + durationMinutes` üzerinden türetilir. Legacy kaydın otomatik olarak aktif yapılmaması [[RISK_REGISTER_GRAPH|R8]] kapanış koşuludur.

## Sonuçlar

- Home yalnızca tamamlanan seansları toplam/hafta/son seans metriklerine dahil eder; aktif seansı ayrı CTA verisi olarak taşır.
- Aktif seans varsa Home ve Workout CTA’ları aynı ID’ye devam eder.
- Tamamlanan seansın set ekleme, set silme veya yeniden bitirme girişimi repository/DAO sınırında reddedilir.
- `CANCELLED` bu dilime alınmamıştır; açık bir ürün davranışı ve ayrı migration gerektirir.

## Kabul

- [x] Açık `5 → 6` migration ve v6 schema export.
- [x] Legacy satırlar korunur ve `COMPLETED` olur.
- [x] Eşzamanlı start/resume çağrıları tek aktif seans döndürür.
- [x] Finish sonrasında aktif Flow boşalır ve geçmiş detay salt okunur olur.
- [x] Home CTA aktif seansı doğrudan açar.
- [x] Light/dark ve %200 font Compose/görsel kontrolü.

Bağlar: [[FEATURE_WORKOUT]], [[FEATURE_HOME]], [[ARCH_DATABASE]], [[ARCH_STATE_DATA_FLOW]], [[TEST_MATRIX]].
