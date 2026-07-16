---
aliases: [GymTracker Graph Start, Başlangıç]
type: moc
status: current
implementation_status: mixed
verified_commit: 341bf79
updated: 2026-07-15
tags: [gymtracker/moc, gymtracker/graph]
---

# GymTracker — Graf Başlangıcı

> [!important]
> Yeni geliştirme oturumunda önce [[SESSION_HANDOFF]], sonra ilgili MOC ve feature düğümü okunur. Bütün depoyu yeniden taramak yerine [[CHANGE_IMPACT_MAP]] üzerinden hedef dosyalar seçilir.

## 30 saniyelik proje özeti

- Android/Kotlin, tek `:app` modülü
- Jetpack Compose + Navigation Compose
- Room v6 + Hilt + KSP
- Local-first; backend ve gerçek ödeme yok
- 5 ana navigation hedefi, 3 İlerleme alt hedefi, ID tabanlı workout/program detayları
- 82 benzersiz egzersiz, 108 hazır program egzersiz referansı
- Debug build başarılı; 20 JVM + 33 cihaz testi geçiyor; lint 0 hata/67 uyarı
- Destructive migration, yeni fotoğraf URI kaybı, seed race ve temel navigation/state riskleri kapatıldı veya azaltıldı

## Ana haritalar

- [[MOC_ARCHITECTURE]] — mevcut ve hedef katmanlar
- [[MOC_FEATURES]] — ekran/özellik düğümleri
- [[MOC_DATA]] — Room modeli, DAO ve migration
- [[MOC_DESIGN]] — modern UI/UX ve bileşenler
- [[MOC_QUALITY_RISKS]] — P0–P3 risk ve testler
- [[MOC_ROADMAP]] — faz, sprint ve PR sırası

## Oturum araçları

- [[AGENTS]] — gelecekteki Codex oturumlarının zorunlu çalışma anlaşması
- [[SESSION_START_PROTOCOL]] — yeni oturumda izlenecek sıra
- [[SESSION_HANDOFF]] — son durum, sıradaki iş ve blokaj
- [[SOURCE_FILE_ATLAS]] — dosya → sorumluluk haritası
- [[CHANGE_IMPACT_MAP]] — değişiklik → etkilenen dosya/test/not
- [[GRAPH_MAINTENANCE]] — grafı kodla senkron tutma
- [[ADR_INDEX]] — mimari kararlar

## Ana raporlar

- [[PROJE_ANALIZ_RAPORU]] — ayrıntılı mevcut durum analizi
- [[GELISTIRME_MASTER_PLANI]] — ürün, tasarım ve uygulama planı

## Hızlı yönlendirme

| Yapılacak iş | İlk okunacak düğüm |
|---|---|
| Build/lint/dependency | [[ARCH_BUILD_DI]] |
| Room/entity/migration | [[ARCH_DATABASE]] |
| Navigasyon | [[ARCH_NAVIGATION]] |
| Reaktif state | [[ARCH_STATE_DATA_FLOW]] |
| Home | [[FEATURE_HOME]] |
| Antrenman | [[FEATURE_WORKOUT]] |
| Beslenme | [[FEATURE_NUTRITION]] |
| Ölçüm | [[FEATURE_MEASUREMENTS]] |
| Fotoğraf | [[FEATURE_PROGRESS_PHOTOS]] |
| Program | [[FEATURE_PROGRAMS]] |
| Kas haritası | [[FEATURE_MUSCLE_LOAD]] |
| Tasarım sistemi | [[DESIGN_SYSTEM_GRAPH]] |

## Grafın güncellik durumu

- Kod baseline: `341bf79` (`codex/nutrition-measurements`, stacked draft PR #2)
- Kod değişikliği: PR10 Progress Photos + Muscle Load `codex/progress-load` çalışma ağacında tamamlandı; henüz commit/push edilmedi
- Dokümanlar: PR10 backup/reconciliation, completed-set Flow ve adaptive Progress çalışma ağacıyla senkron
- Son tam doğrulama: 2026-07-15
- Aktif önerilen iş: PR10’u incele/commit/push et; ardından [[FEATURE_PROGRAMS]] normalizasyon ve entegrasyon dilimine geç
