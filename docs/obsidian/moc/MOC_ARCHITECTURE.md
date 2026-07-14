---
type: moc
status: current
implementation_status: mixed
verified_commit: a8e019e
updated: 2026-07-14
tags: [gymtracker/moc, gymtracker/architecture]
---

# MOC — Mimari

## Mevcut sistem

- [[ARCH_CURRENT_SYSTEM]] — çalışan paket/bağımlılık grafı
- [[ARCH_NAVIGATION]] — mevcut rotalar ve hedef shell
- [[ARCH_STATE_DATA_FLOW]] — DAO → ViewModel → Compose akışı
- [[ARCH_DATABASE]] — entity/DAO/ilişki grafı
- [[ARCH_BUILD_DI]] — Gradle, Hilt, toolchain ve manifest

## Hedef sistem

- [[ARCH_TARGET_SYSTEM]] — repository/use case sınırı
- [[DESIGN_SYSTEM_GRAPH]] — UI katman standardı
- [[TEST_MATRIX]] — mimariyi koruyan kalite kapısı

## Mimari kararlar

- [[ADR_001_LOCAL_FIRST_SINGLE_MODULE]]
- [[ADR_002_FIVE_TARGET_NAVIGATION]]
- [[ADR_003_EXPLICIT_ROOM_MIGRATIONS]]
- [[ADR_004_PERSISTENT_PHOTO_STORAGE]]
- [[ADR_005_MUSCLE_LOAD_NAMING]]

## Kritik bağlar

`MainActivity` bütün feature ekranlarını doğrudan bağlar. ViewModel’ler repository yerine DAO alır. Room entity’leri UI’a kadar taşınır. Bu yüzden bir entity değişikliği [[CHANGE_IMPACT_MAP#Room entity veya kolon değişikliği]] akışını tetikler.
