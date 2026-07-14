---
type: moc
status: current
implementation_status: current
verified_commit: a8e019e
updated: 2026-07-15
tags: [gymtracker/moc, gymtracker/risk, gymtracker/quality]
---

# MOC — Kalite ve Riskler

- [[RISK_REGISTER_GRAPH]] — P0–P3 risk düğümleri
- [[TEST_MATRIX]] — feature/değişiklik/test eşleşmesi
- [[ARCH_BUILD_DI]] — build/lint/toolchain
- [[CHANGE_IMPACT_MAP]] — regresyon alanı
- [[SESSION_HANDOFF]] — aktif blokajlar

## Şu anki kalite sinyali

| Kontrol | Durum |
|---|---|
| Debug compile/APK | Başarılı |
| Unit test | 8/8 başarılı |
| Android test | 15/15 başarılı, API 34 |
| Lint | 0 error, 52 warning |
| Wrapper | Mevcut |
| CI | Yok |
| Migration test | v3→v5 satır koruma geçiyor |

## Beta kapısı

- Destructive migration yok
- Fotoğraf restart/reboot sonrası açılır
- Lint error 0
- Wrapper + CI yeşil
- Kritik migration/DAO/use case/UI akışları testli
- Aktif workout process recreation sonrası korunur
