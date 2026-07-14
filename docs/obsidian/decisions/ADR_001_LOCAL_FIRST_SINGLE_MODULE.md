---
type: adr
adr: 1
status: accepted
implementation_status: current
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/architecture]
---

# ADR 001 — Local-first ve Tek Modül

## Bağlam

Uygulama bugün yalnızca yerel Room verisi kullanıyor; ekip/ölçek küçük ve tek `:app` modülü var.

## Karar

İlk stabil beta local-first kalacak ve paket sınırları tek modül içinde güçlendirilecek. Backend, hesap ve cloud sync veri/migration omurgası oturmadan eklenmeyecek. Multi-module ancak ekip paralelliği veya ölçülmüş build süresi gerekçesiyle değerlendirilecek.

## Sonuç

- Daha hızlı stabilizasyon, az operasyonel kapsam.
- Repository sınırı yine kurulacak.
- Sync gelirse public/sync ID ve conflict modeli ayrıca tasarlanacak.

Bağlar: [[ARCH_TARGET_SYSTEM]], [[MOC_ROADMAP]].
