---
type: workflow
status: current
implementation_status: active
updated: 2026-07-14
tags: [gymtracker/workflow, gymtracker/graph]
---

# Graf Bakım Protokolü

## Ne zaman güncellenir?

- Kaynak dosya ekleme/silme/taşıma
- Route veya dependency edge değişimi
- Entity/DAO/schema değişimi
- Feature davranışı veya risk kapanışı
- ADR kararı kabul/red/supersede
- Sprint/PR tamamlanması

## Minimum güncelleme

1. İlgili feature/architecture note
2. [[SOURCE_FILE_ATLAS]] gerekiyorsa
3. [[CHANGE_IMPACT_MAP]] yeni çapraz etki varsa
4. [[RISK_REGISTER_GRAPH]] risk durumu
5. [[SESSION_HANDOFF]]
6. `verified_commit` ve `updated`

## Stale algılama

- Not `verified_commit != HEAD` ise stale adayıdır.
- Frontmatter `source_files` yolu yoksa broken node.
- Çözümlenmeyen wikilink orphan/broken edge’dir.
- Canvas file node’u yoksa görsel graph eskidir.

## Kural

Grafı otomatik olarak her küçük satır değişikliğinde şişirme. Yalnız sorumluluk, akış, risk, karar, dosya konumu veya test sözleşmesi değiştiğinde güncelle.
