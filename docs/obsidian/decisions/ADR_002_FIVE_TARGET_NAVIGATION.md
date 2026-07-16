---
type: adr
adr: 2
status: accepted
implementation_status: implemented
date: 2026-07-14
verified_commit: 8a2d350
updated: 2026-07-16
tags: [gymtracker/decision, gymtracker/navigation, gymtracker/design]
---

# ADR 002 — Beş Ana Navigasyon Hedefi

## Bağlam

Mevcut bottom bar 7 öğelidir; dar ekranda sıkışır ve ölçüm/fotoğraf/kas yükü ayrı hedeflerdir.

## Karar

Telefon: Özet, Antrenman, Programlar, Beslenme, İlerleme. Ölçüm/fotoğraf/kas yükü İlerleme altındadır. Aktif workout tam ekran. Medium/expanded: navigation rail + gerektiğinde list-detail.

Dış giriş: `gymtracker://app` custom scheme/host sözleşmesi bütün ürün hedeflerini açar. Detail URI’leri yalnız pozitif Room ID kabul eder; URI üretme/parçalama ve NavType doğrulaması tek `DeepLinkContract` sınırındadır.

## Alternatifler

- 7 hedefi koruma: düşük migration maliyeti, zayıf hiyerarşi.
- “Diğer” menüsü: daha az bar öğesi, core feature keşfi zayıflar.
- Programları Workout içine alma: mümkün, fakat programın ürün değeri görünmezleşir.

## Kabul

- [x] Telefon alt barı tam 5 ana hedef içerir.
- [x] Ölçüm, fotoğraf ve kas yükü İlerleme altında route olarak kalır.
- [x] Workout/program detail yalnız pozitif DB ID taşır ve `SavedStateHandle` ile yeniden çözülür.
- [x] Back, seçili sekme ve Activity recreation Compose testleri geçer.
- [x] Harici deep link sözleşmeleri; manifest çözümü, cold start, detail ID, safe fallback ve recreation testli.
- [x] Uygulama dışı test süreci; arka plan `am kill`, PID değişimi ve aynı Android task üzerinden alt rota/detail restorasyonunu doğrular.
- [x] 600 dp ve üzerinde aynı beş hedefi kullanan navigation rail.
- [x] Workout ve Programs için 840 dp+ ortak list-detail düzenleri.

Bağlar: [[ARCH_NAVIGATION]], [[SCREEN_BLUEPRINT_GRAPH]].
