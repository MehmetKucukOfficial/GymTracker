---
type: design
status: current
implementation_status: mixed
verified_commit: 4d0cad8
updated: 2026-07-15
tags: [gymtracker/design, gymtracker/feature]
---

# Ekran Blueprint Grafı

## Home

Uygulandı: başlat CTA → açıklayıcı empty → haftalık/toplam/nutrition/measurement metrikleri → hızlı işlemler → son workout. Veri yoksa sahte `0` yok; loading/error ayrıdır. Aktif-seans “Devam et” CTA’sı ve gerçek trend datasına dayalı mini grafik [[FEATURE_WORKOUT]] status modelinden sonra eklenecek. Kaynak: [[FEATURE_HOME]].

## Aktif workout

Tam ekran; timer; exercise card; previous/kg/reps/complete set row; add/copy/delete; rest timer; auto-save; finish summary. Kaynak: [[FEATURE_WORKOUT]].

## Programlar

Filter chip + card list; ID detail; week/day accordion; sticky “Programa başla/Devam et/Satın al”. Kaynak: [[FEATURE_PROGRAMS]].

## Beslenme

Date pager; calorie budget; macro progress; dört meal card; recent/favorite food; quick add + undo. Kaynak: [[FEATURE_NUTRITION]].

## İlerleme merkezi

Overview / Measurements / Photos / Muscle Load. Compact scrollable tabs, expanded list-detail. Kaynaklar: [[FEATURE_MEASUREMENTS]], [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]].

## Responsive kural

- Compact: bottom navigation; Home normal fontta iki metrik kolonu, %200 fontta tek kolon
- Medium: navigation rail, 2 kolon uygun yerlerde
- Expanded: rail/drawer + list-detail
- Active workout compact dahil alt barı gizler

PR7’de Home içeriği 720 dp üstünde dört metrik kolonuna geçer; navigation rail/drawer uygulama shell’i henüz uygulanmadı.

Tam ekran wireframe ve token’lar: [[GELISTIRME_MASTER_PLANI#7. Ekran bazlı UX/UI planı]].
