---
aliases: [GymTracker Graph Start, Başlangıç]
type: moc
status: current
implementation_status: mixed
verified_commit: e28a22d
updated: 2026-07-16
tags: [gymtracker/moc, gymtracker/graph]
---

# GymTracker — Graf Başlangıcı

> [!important]
> Yeni geliştirme oturumunda önce [[SESSION_HANDOFF]], sonra ilgili MOC ve feature düğümü okunur. Bütün depoyu yeniden taramak yerine [[CHANGE_IMPACT_MAP]] üzerinden hedef dosyalar seçilir.

## 30 saniyelik proje özeti

- Android/Kotlin, tek `:app` modülü
- Jetpack Compose + Navigation Compose
- Room v9 + Hilt + KSP
- Local-first; backend ve gerçek ödeme yok
- 5 ana navigation hedefi, 3 İlerleme alt hedefi, ID tabanlı workout/program detayları
- 82 benzersiz egzersiz, 108 hazır program egzersiz referansı
- `ciQuality` debug + minified release APK/AAB üretir; 30 JVM + 62 cihaz testi geçiyor; lint 0 hata
- Workout ve Programs compact’ta ID rotası, 840 dp+ genişlikte ortak liste–özet kullanır; dört kalıcı golden baseline vardır
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

- Master code baseline: `3f2f8cf`; GitHub PR #1–#11 merge commit ile sırayla birleştirildi, açık PR kalmadı ve kaynak dallar korundu
- PR #11, CI push hedefini varsayılan `master` dalıyla eşledi; otomatik master koşusu `29499237460` tamamen yeşil
- Aktif PR18 code baseline: `e28a22d` (`codex/legacy-photo-recovery`); v0 yeniden seçim ve v1 yeniden optimizasyon yerel kabulü tamamlandı
- Dokümanlar: PR18 feature/ADR/test/risk/roadmap/handoff durumuyla senkron
- Son tam doğrulama: 2026-07-16
- Aktif önerilen iş: `ciQuality` kapısını çalıştır, PR18’i yayınla ve hosted kontrolleri doğrula
