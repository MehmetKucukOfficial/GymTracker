---
type: adr
adr: 2
status: accepted
implementation_status: partial
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/navigation, gymtracker/design]
---

# ADR 002 — Beş Ana Navigasyon Hedefi

## Bağlam

Mevcut bottom bar 7 öğelidir; dar ekranda sıkışır ve ölçüm/fotoğraf/kas yükü ayrı hedeflerdir.

## Karar

Telefon: Özet, Antrenman, Programlar, Beslenme, İlerleme. Ölçüm/fotoğraf/kas yükü İlerleme altındadır. Aktif workout tam ekran. Medium/expanded: navigation rail + gerektiğinde list-detail.

## Alternatifler

- 7 hedefi koruma: düşük migration maliyeti, zayıf hiyerarşi.
- “Diğer” menüsü: daha az bar öğesi, core feature keşfi zayıflar.
- Programları Workout içine alma: mümkün, fakat programın ürün değeri görünmezleşir.

## Kabul

- [x] Telefon alt barı tam 5 ana hedef içerir.
- [x] Ölçüm, fotoğraf ve kas yükü İlerleme altında route olarak kalır.
- [x] Workout/program detail yalnız pozitif DB ID taşır ve `SavedStateHandle` ile yeniden çözülür.
- [x] Back, seçili sekme ve Activity recreation Compose testleri geçer.
- [ ] Harici deep link sözleşmeleri.
- [ ] Medium/expanded navigation rail ve list-detail düzeni.

Bağlar: [[ARCH_NAVIGATION]], [[SCREEN_BLUEPRINT_GRAPH]].
