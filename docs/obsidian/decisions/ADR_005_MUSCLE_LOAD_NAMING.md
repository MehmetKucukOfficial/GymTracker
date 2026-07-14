---
type: adr
adr: 5
status: proposed
implementation_status: not-started
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/workload]
---

# ADR 005 — Yorgunluk Yerine Kas Yükü

## Bağlam

Mevcut hesap yalnızca set dış yükünü sabit activation ve threshold ile dağıtır; fizyolojik toparlanmayı ölçmez.

## Karar

UI adı “7 Günlük Kas Yükü”. Yüzde kesinliği yerine Düşük/Orta/Yüksek etiketleri ve ayrıntıda skor. Metodoloji bilgi sheet’inde formül ve sınırlar açıklanır.

## Hesap geliştirme

- Yalnız completed set
- Bodyweight stratejisi
- Time decay
- Stable exercise ID
- Bilinmeyen exercise telemetry/debug signal
- İleri fazda RPE/RIR ve kişisel baseline

## Kabul

Formula unit testli; yeni set reaktif; harita yanında erişilebilir liste ve legend var.

Bağlar: [[FEATURE_MUSCLE_LOAD]], [[FEATURE_WORKOUT]].
