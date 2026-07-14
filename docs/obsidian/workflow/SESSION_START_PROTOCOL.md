---
type: workflow
status: current
implementation_status: active
updated: 2026-07-14
tags: [gymtracker/workflow, gymtracker/session]
---

# Oturum Başlangıç Protokolü

## Amaç

Yeni geliştirme oturumunda bütün Kotlin dosyalarını yeniden taramadan doğru düğüm ve dosya kümesine inmek.

## Zorunlu sıra

1. `git status --short --branch` ve `git rev-parse --short HEAD`.
2. [[SESSION_HANDOFF]] oku: aktif faz, sıradaki PR, blokaj, son test.
3. Handoff’taki `primary_nodes` notlarını oku.
4. [[CHANGE_IMPACT_MAP]] içinden değişiklik tipini seç.
5. Yalnızca düğümün `source_files` alanı ve etki matrisindeki ek dosyaları doğrula.
6. Notun `verified_commit` değeri HEAD’den farklıysa ilgili dosyalarda hedefli `rg`/diff yap.
7. Uygulama ve orantılı test.
8. İlgili feature/architecture/ADR notunu ve [[SESSION_HANDOFF]] güncelle.

## Güven sınırı

Graf “nereden başlanacağını” söyler; kodun o anki davranışına dair son sözü söylemez. Özellikle dirty worktree, yeni migration veya başka agent değişikliği varsa targeted source check atlanmaz.

## Oturum bitişi

- Neler değişti?
- Hangi testler ve sonuçları?
- Hangi risk kapandı/açıldı?
- Sıradaki tek somut iş?
- `verified_commit` hangi notlarda güncellendi?

Bu beş madde [[SESSION_HANDOFF]] içine yazılır.
