---
type: design
status: current
implementation_status: mixed
verified_commit: 7917358
updated: 2026-07-15
tags: [gymtracker/design, gymtracker/feature]
---

# Ekran Blueprint Grafı

## Home

Uygulandı: başlat CTA → açıklayıcı empty → haftalık/toplam/nutrition/measurement metrikleri → hızlı işlemler → son workout. Veri yoksa sahte `0` yok; loading/error ayrıdır. Aktif-seans “Devam et” CTA’sı ve gerçek trend datasına dayalı mini grafik [[FEATURE_WORKOUT]] status modelinden sonra eklenecek. Kaynak: [[FEATURE_HOME]].

## Aktif workout

Tam ekran; timer; exercise card; previous/kg/reps/complete set row; add/copy/delete; rest timer; auto-save; finish summary. Kaynak: [[FEATURE_WORKOUT]].

## Programlar

Uygulandı: adaptive program kart grid’i; free/demo erişim badge’i; ID detail; ilişkisel gün accordion’ı; gün bazlı “Bu antrenmana başla”; locked/empty/error state; light/dark/%200 font. Program günü gerçek ACTIVE workout’a planlanan setler olarak aktarılır. Filtreleme, program ilerleme takvimi ve expanded list-detail sonraki üründür. Kaynak: [[FEATURE_PROGRAMS]].

## Beslenme

Uygulandı: locale-aware date pager; calorie budget; adaptive macro progress; dört meal card; snapshot-safe miktar/meal edit ve delete Snackbar undo. Recent/favorite food ve quick add sonraki ürün. Kaynak: [[FEATURE_NUTRITION]].

## İlerleme merkezi

Uygulandı: token tabanlı Overview kartları; Measurements latest/edit/delete-undo + 7/30/90/tümü gerçek kilo grafiği + özel tarih; adaptive photo grid + scrollable angle filter + viewer; completed-set kaynaklı kas haritası + renk bağımsız erişilebilir liste/metodoloji. Expanded list-detail, çoklu ölçüm metriği ve kalıcı karşılaştırma seçimi sonraki aşamadır. Kaynaklar: [[FEATURE_MEASUREMENTS]], [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]].

## Responsive kural

- Compact: bottom navigation; Home normal fontta iki metrik kolonu, %200 fontta tek kolon
- Medium: navigation rail, 2 kolon uygun yerlerde
- Expanded: rail/drawer + list-detail
- Active workout compact dahil alt barı gizler

PR7’de Home içeriği 720 dp üstünde dört metrik kolonuna geçer; navigation rail/drawer uygulama shell’i henüz uygulanmadı.

Tam ekran wireframe ve token’lar: [[GELISTIRME_MASTER_PLANI#7. Ekran bazlı UX/UI planı]].
