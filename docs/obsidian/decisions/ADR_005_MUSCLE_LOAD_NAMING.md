---
type: adr
adr: 5
status: accepted
implementation_status: current-core
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/workload]
---

# ADR 005 — Yorgunluk Yerine Kas Yükü

## Bağlam

Mevcut hesap yalnızca set dış yükünü sabit activation ve threshold ile dağıtır; fizyolojik toparlanmayı ölçmez.

## Karar

UI adı “7 Günlük Kas Yükü”. Yüzde kesinliği yerine Düşük/Orta/Yüksek etiketleri ve ayrıntıda skor. Metodoloji bilgi sheet’inde formül ve sınırlar açıklanır.

## Hesap geliştirme

- Yalnız completed set — uygulandı
- Bodyweight stratejisi — 20 kg açık fallback ile ilk sürüm uygulandı
- Time decay — yedi günde 1.0→0.5 lineer ağırlık uygulandı
- Stable exercise ID
- Bilinmeyen exercise debug/UI sayacı — uygulandı
- İleri fazda RPE/RIR ve kişisel baseline

## Kabul

Formula unit testli; completed set Room Flow’u reaktif; harita yanında erişilebilir liste, legend, normalize skor ve metodoloji dialogu var. Stable ID, kişisel bodyweight, RPE/RIR ve baseline sonraki revizyondur.

Bağlar: [[FEATURE_MUSCLE_LOAD]], [[FEATURE_WORKOUT]].
