---
aliases: [GymTracker Graph Start, Başlangıç]
type: moc
status: current
implementation_status: mixed
verified_commit: 81cb7fe
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
- `ciQuality` debug + minified release APK/AAB üretir; PR25 dalında 42 JVM, hedefli 19 export cihaz testi; PR24 hosted kapısında 80 app + 2 gerçek reboot fazı + 4 gerçek process-death + 1 grid macrobenchmark geçiyor; lint 0 hata
- Format v1 manuel ZIP export on bir Room tablosunu, tema tercihini ve yönetilen fotoğrafları taşır; staged restore ve tüm veriyi silme sıradadır
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
| Ayarlar/veri yönetimi | [[FEATURE_SETTINGS_DATA]] |

## Grafın güncellik durumu

- Master code baseline: `80c79b5`; GitHub PR #1–#18 merge commit ile sırayla birleştirildi ve kaynak dallar korundu
- PR21 hosted koşusu `29764819516` tamamen yeşil; artifact XML 71 app + 2 process-death testi gösteriyor
- PR22 hosted retry `29769324578` tamamen yeşil; artifact XML 71 app + 4 process-death testi gösteriyor ve GitHub PR #16 merge commit `79a80f4` ile birleştirildi
- PR23 hosted koşusu `29771389567` tamamen yeşil; artifact XML 75 app + 5 benchmark/process testi, reboot seed/verify 1/1 + 1/1, beş benchmark tekrarı ve beş Perfetto trace gösteriyor; merge commit `ce20615`
- PR24 hosted koşusu `29775858318` tamamen yeşil; artifact XML 80 app + 5 benchmark/process, reboot 1+1 ve beş trace; merge commit `80c79b5`
- Aktif PR25 integration head `81cb7fe` (`codex/data-export`); format v1 manuel export, tam `ciQuality`, 42 JVM, hedefli 19/19 ve tam 85/85 API 34 testi geçti
- Dokümanlar: PR24 master ve PR25 manuel veri dışa aktarma sözleşmesiyle senkron
- Son tam doğrulama: 2026-07-20
- Aktif önerilen iş: PR25 tam 85-test cihaz kabulünü çalıştır, hosted artifact kabulünden sonra merge commit ile birleştir; ardından staged restore dilimine geç
