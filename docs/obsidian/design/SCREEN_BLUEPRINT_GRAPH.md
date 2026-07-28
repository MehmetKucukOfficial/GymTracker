---
type: design
status: current
implementation_status: mixed
verified_commit: 7e9d4c0
updated: 2026-07-28
tags: [gymtracker/design, gymtracker/feature]
---

# Ekran Blueprint Grafı

## Home

Uygulandı: başlat CTA → açıklayıcı empty → haftalık/toplam/nutrition/measurement metrikleri → hızlı işlemler → son workout. Veri yoksa sahte `0` yok; loading/error ayrıdır. Aktif-seans “Devam et” CTA’sı ve gerçek trend datasına dayalı mini grafik [[FEATURE_WORKOUT]] status modelinden sonra eklenecek. Kaynak: [[FEATURE_HOME]].

## Antrenman

Uygulandı: compact aktif/geçmiş liste → ID tabanlı tam ekran detail; 840 dp+ seçili seans özeti; planned/kg/reps completion; add/delete; auto-save; read-only completed summary; bütün loading/content/not-found durumlarında ortak görünür geri top bar. Timer, previous values, edit/undo, rest timer ve gelişmiş finish summary sonraki üründür. Kaynak: [[FEATURE_WORKOUT]].

## Programlar

Uygulandı: adaptive program kart grid’i; 840 dp+ seçili program özeti; free/demo erişim badge’i; ID detail; ortak görünür geri top bar/Snackbar; ilişkisel gün accordion’ı; gün bazlı “Bu antrenmana başla”; locked/empty/error state; light/dark/%200 font. Program günü gerçek ACTIVE workout’a planlanan setler olarak aktarılır. Filtreleme ve program ilerleme takvimi sonraki üründür. Kaynak: [[FEATURE_PROGRAMS]].

## Beslenme

Uygulandı: locale-aware date pager; calorie budget; adaptive macro progress; dört meal card; snapshot-safe miktar/meal edit ve ortak Snackbar üzerinden delete undo; Turkish dark/%200 over-goal golden. Recent/favorite food ve quick add sonraki ürün. Kaynak: [[FEATURE_NUTRITION]].

## İlerleme merkezi

Uygulandı: token tabanlı Overview kartları; Measurements latest/edit/delete-undo + 7/30/90/tümü gerçek kilo grafiği + özel tarih; adaptive photo grid + scrollable angle filter + viewer; completed-set kaynaklı kas haritası + renk bağımsız erişilebilir liste/metodoloji. Üç child rota ortak büyük-font uyumlu top bar, tek geri semantics’i ve sistem back sözleşmesini kullanır. Measurements English/light chart, Photos Turkish dark/%200 saving, Muscle Load English/light content ve Turkish dark/%200 loading golden’ları kalıcıdır; fotoğraf mutation’ları kayıt sırasında kilitlidir. Expanded list-detail, çoklu ölçüm metriği ve kalıcı karşılaştırma seçimi sonraki aşamadır. Kaynaklar: [[FEATURE_MEASUREMENTS]], [[FEATURE_PROGRESS_PHOTOS]], [[FEATURE_MUSCLE_LOAD]].

## Responsive kural

- Compact: bottom navigation; Home normal fontta iki metrik kolonu, %200 fontta tek kolon
- Medium: navigation rail, 2 kolon uygun yerlerde
- Expanded 840 dp+: rail + Workout/Programs list-detail
- Active workout compact dahil alt barı gizler

Home içeriği 720 dp üstünde dört metrik kolonuna geçer; shell 600 dp+ rail, Workout/Programs ise 840 dp+ list-detail kullanır. Drawer henüz gerekli görülmedi.

Tam ekran wireframe ve token’lar: [[GELISTIRME_MASTER_PLANI#7. Ekran bazlı UX/UI planı]].
