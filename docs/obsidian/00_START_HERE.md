---
aliases: [GymTracker Graph Start, Başlangıç]
type: moc
status: current
implementation_status: mixed
verified_commit: 74b6d82
updated: 2026-07-28
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
- `ciQuality` debug + minified release APK/AAB üretir; PR29 yerel kapısında 48 JVM, 100 API 34 normal test, gerçek restore process-kill ve Android backup transport host kabulleri geçer; lint 0 hata
- Format v1 manuel ZIP export, strict staged restore/rollback ve güçlü onaylı tüm-veri silme on bir Room tablosu, tema ve yönetilen fotoğraf sözleşmesini kapsar; PR26 hosted kabulü tamamlandı
- Workout ve Programs compact’ta ID rotası, 840 dp+ genişlikte ortak liste–özet kullanır; on kalıcı golden baseline vardır
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

- Master code baseline: `6083210`; GitHub PR #1–#22 merge commit ile sırayla birleştirildi ve kaynak dallar korundu
- PR21 hosted koşusu `29764819516` tamamen yeşil; artifact XML 71 app + 2 process-death testi gösteriyor
- PR22 hosted retry `29769324578` tamamen yeşil; artifact XML 71 app + 4 process-death testi gösteriyor ve GitHub PR #16 merge commit `79a80f4` ile birleştirildi
- PR23 hosted koşusu `29771389567` tamamen yeşil; artifact XML 75 app + 5 benchmark/process testi, reboot seed/verify 1/1 + 1/1, beş benchmark tekrarı ve beş Perfetto trace gösteriyor; merge commit `ce20615`
- PR24 hosted koşusu `29775858318` tamamen yeşil; artifact XML 80 app + 5 benchmark/process, reboot 1+1 ve beş trace; merge commit `80c79b5`
- PR25/GitHub PR #19 hosted run `29780305582` artifact denetiminden geçti ve merge commit `1c3e193` ile `master`a alındı
- PR26/GitHub PR #20 hosted run `29783838425` artifact denetiminden geçti ve merge commit `a370f82` ile `master`a alındı; kaynak dal korundu
- PR27/GitHub PR #21 hosted run `30375520329` tamamen yeşil; artifact XML 100 app + 5 benchmark/process, reboot 1+1 ve beş trace gösteriyor; merge commit `7a79978`
- PR28/GitHub PR #22 hosted run `30384849616` tamamen yeşil; artifact XML 100 app + 5 benchmark/process, reboot 1+1, restore seed/kill/verify ve beş trace gösteriyor; merge commit `6083210`
- PR29/GitHub PR #23 hosted run `30389433873` tamamen yeşil; artifact XML 100 app + 5 benchmark/process, reboot 1+1, restore-kill, Android transport seed/backup/restore/verify ve beş trace gösteriyor; final head `74b6d82`
- Dokümanlar: PR28 master kabulü ve PR29 hosted backup transport kabulüyle senkron
- Son tam doğrulama: 2026-07-28
- Aktif önerilen iş: PR29/GitHub PR #23’ü ready yap ve kaynak dalı koruyarak merge commit ile birleştir
