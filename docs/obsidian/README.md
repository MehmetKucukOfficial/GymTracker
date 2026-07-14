---
type: guide
status: current
updated: 2026-07-14
tags: [gymtracker/graph, gymtracker/workflow]
---

# GymTracker Obsidian Grafı

Bu klasör, GymTracker kaynak kodunun hızlı okunabilir bilgi grafıdır. İki desteklenen kullanım vardır:

1. Depo kökünü bağımsız vault olarak açmak.
2. Ana `Çalışmalar` kasasında `GymTracker/Depo` junction’ı üzerinden kullanmak. Ana kasa için giriş notu `GymTracker/Geliştirme Merkezi.md`, uyarlanmış Canvas ise `GymTracker/GymTracker Proje Grafı.canvas` dosyasıdır.

Başlangıç düğümü: [[00_START_HERE]]
Aktif çalışma durumu: [[SESSION_HANDOFF]]
Oturum protokolü: [[SESSION_START_PROTOCOL]]
Görsel canvas: [[GymTracker.canvas]]

Depo kökündeki [[AGENTS]] dosyası gelecek Codex oturumlarını bu grafa yönlendirir.

## Önerilen Graph View ayarı

- Bağımsız repo vault filtresi: `path:"docs/obsidian" OR file:"PROJE_ANALIZ_RAPORU" OR file:"GELISTIRME_MASTER_PLANI"`
- Çalışmalar kasası filtresi: `path:"GymTracker/Depo/docs/obsidian" OR path:"GymTracker/Geliştirme Merkezi" OR path:"GymTracker/GymTracker"`
- Attachments: kapalı
- Orphans: kapalı
- Tags: açık
- Arrows: açık

Önerilen gruplar:

| Arama | Renk amacı |
|---|---|
| `tag:#gymtracker/moc` | Harita/MOC düğümleri |
| `tag:#gymtracker/feature` | Ürün özellikleri |
| `tag:#gymtracker/architecture` | Mimari |
| `tag:#gymtracker/data` | Veri |
| `tag:#gymtracker/design` | Tasarım |
| `tag:#gymtracker/risk` | Riskler |
| `tag:#gymtracker/decision` | ADR/kararlar |
| `tag:#gymtracker/workflow` | Oturum ve geliştirme akışı |

## Doğruluk kuralı

Graf kaynak kodun yerine geçmez. Bir notun `verified_commit` değeri mevcut `HEAD` ile aynı değilse önce o notun `source_files` listesindeki dosyalar hedefli olarak doğrulanır. Değişiklikten sonra ilgili not ve [[SESSION_HANDOFF]] güncellenir.
