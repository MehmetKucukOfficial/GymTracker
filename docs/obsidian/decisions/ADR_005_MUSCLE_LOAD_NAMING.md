---
type: adr
adr: 5
status: accepted
implementation_status: current-calibration
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
- Bodyweight stratejisi — en güncel geçerli BodyMeasurement; veri yoksa açık 20 kg fallback uygulandı
- Time decay — yedi günde 1.0→0.5 lineer ağırlık uygulandı
- Stable exercise ID
- Bilinmeyen exercise debug/UI sayacı — uygulandı
- İleri fazda RPE/RIR ve kişisel baseline

## Kabul

Formula unit testli; completed set Room Flow’u en güncel geçerli measurement Flow’u ile birleşir; harita yanında erişilebilir liste, legend, normalize skor, kullanılan kişisel/varsayılan kalibrasyon ve metodoloji dialogu vardır. Load projeksiyonunda stable exercise ID, hareket türüne özel bodyweight katsayısı, RPE/RIR ve kişisel haftalık baseline sonraki revizyondur.

Bağlar: [[FEATURE_MUSCLE_LOAD]], [[FEATURE_WORKOUT]].
