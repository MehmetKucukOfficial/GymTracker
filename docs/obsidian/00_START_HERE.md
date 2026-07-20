---
aliases: [GymTracker Graph Start, Başlangıç]
type: moc
status: current
implementation_status: mixed
verified_commit: 26696b9
updated: 2026-07-20
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
- `ciQuality` debug + minified release APK/AAB üretir; 37 JVM + 75 app cihaz + 2 gerçek reboot fazı + 4 gerçek process-death + 1 grid macrobenchmark geçiyor; lint 0 hata
- Workout ve Programs compact’ta ID rotası, 840 dp+ genişlikte ortak liste–özet kullanır; altı kalıcı golden baseline vardır
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

- Master code baseline: `39927a7`; GitHub PR #1–#15 merge commit ile sırayla birleştirildi ve kaynak dallar korundu
- PR21 hosted koşusu `29764819516` tamamen yeşil; artifact XML 71 app + 2 process-death testi gösteriyor
- Aktif PR22 head: `a4f7bd9` (`codex/state-time-resilience`, GitHub PR #16); SavedState/Clock/ZoneId kabulü ve boş-DB process-death kararlılık düzeltmesi yerelde geçti, hosted retry `29769324578` sürüyor
- Aktif PR23 code baseline: `26696b9` (`codex/photo-device-resilience`); 48 MB preflight, backup/restore sözleşmesi, gerçek reboot ve 120-row grid benchmark yerel kabulden geçti
- Dokümanlar: PR21 master, PR22 retry ve PR23 fotoğraf cihaz dayanıklılığı koduyla senkron
- Son tam doğrulama: 2026-07-20
- Aktif önerilen iş: PR22 hosted retry artifact’ini doğrula ve merge et; PR23 graph commit’ini yayınlayıp hosted reboot/benchmark kabulüne geçir
