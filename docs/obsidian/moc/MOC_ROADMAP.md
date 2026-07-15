---
type: moc
status: current
implementation_status: active
verified_commit: 5c3ce35
updated: 2026-07-16
tags: [gymtracker/moc, gymtracker/roadmap]
---

# MOC — Yol Haritası

- [[ROADMAP_EXECUTION_GRAPH]] — PR ve sprint bağımlılık grafı
- [[GELISTIRME_MASTER_PLANI#18. Fazlar ve süre tahmini]]
- [[GELISTIRME_MASTER_PLANI#19. Önerilen 2 haftalık sprint planı]]
- [[SESSION_HANDOFF]] — bugün başlanacak iş

## Ana sıra

```mermaid
flowchart LR
    P1[Build hygiene] --> P2[Test foundation]
    P2 --> P3[DB safety]
    P3 --> P4[Seed safety]
    P3 --> P5[Photo storage]
    P4 --> P6[Navigation + state]
    P5 --> P6
    P6 --> DS[Design system + Home]
    DS --> WO[Workout redesign]
    WO --> REST[Other features]
    REST --> MEDIA[Photo media + load calibration]
    MEDIA --> BETA
```

Yeni oturum [[SESSION_START_PROTOCOL]] ile başlamalı ve tamamlanan PR sonrasında [[SESSION_HANDOFF]] güncellenmelidir.
