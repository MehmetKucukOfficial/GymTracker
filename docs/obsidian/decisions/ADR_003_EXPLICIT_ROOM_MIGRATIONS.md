---
type: adr
adr: 3
status: accepted
implementation_status: partial
date: 2026-07-14
tags: [gymtracker/decision, gymtracker/data, gymtracker/risk/p0]
---

> Implementation update (2026-07-14): Version 3 is the oldest verifiable schema in Git and on the development emulator. Schema export is enabled, destructive fallback is removed, `3.json` is versioned, and the v3 baseline preservation test passes on API 34. No v1/v2 schema artifact or Git history exists, so SQL for those versions will not be invented. Supported migrations begin at v3; an unknown older database fails closed instead of being deleted.

# ADR 003 — Açık Room Migration

## Bağlam

DB v3, şema export kapalı ve destructive fallback açık. Üretim verisi schema değişiminde silinebilir.

## Karar

`exportSchema=true`; schema JSON version control; her version edge için açık Migration; destructive fallback kaldırılır; `MigrationTestHelper` ve gerçekçi fixture kullanılır.

## Sonuç

- Entity değişikliği migration olmadan merge edilmez.
- Seed migration’dan ayrılır.
- CI eski desteklenen bütün sürümlerden latest’e test yapar.

## Kabul

Satırlar ve kritik alanlar v1/v2/v3→latest korunur; downgrade davranışı ayrıca belgelenir.

Bağlar: [[ARCH_DATABASE]], [[TEST_MATRIX#Room ve migration]], [[RISK_REGISTER_GRAPH]].
