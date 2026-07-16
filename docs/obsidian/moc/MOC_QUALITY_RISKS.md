---
type: moc
status: current
implementation_status: current
verified_commit: a2586d1
updated: 2026-07-16
tags: [gymtracker/moc, gymtracker/risk, gymtracker/quality]
---

# MOC — Kalite ve Riskler

- [[RISK_REGISTER_GRAPH]] — P0–P3 risk düğümleri
- [[TEST_MATRIX]] — feature/değişiklik/test eşleşmesi
- [[PERFORMANCE_BASELINE]] — startup/scroll/navigation ölçüm sözleşmesi
- [[ARCH_BUILD_DI]] — build/lint/toolchain
- [[CHANGE_IMPACT_MAP]] — regresyon alanı
- [[SESSION_HANDOFF]] — aktif blokajlar

## Şu anki kalite sinyali

| Kontrol | Durum |
|---|---|
| Debug compile/APK | Başarılı |
| Unit test | 28/28 başarılı |
| Android test | 54/54 başarılı, API 34 |
| Profil üretici | 2/2 başarılı |
| Macrobenchmark | 4 senaryo çalışıyor; fiziksel cihaz kabulü açık |
| Lint | 0 error |
| Wrapper | Mevcut |
| CI | quality + API 34 hosted yeşil; production release manuel/protected |
| Migration test | v3→v9 satır koruma geçiyor |

## Beta kapısı

- Destructive migration yok
- Fotoğraf restart/reboot sonrası açılır
- Lint error 0
- Wrapper + CI yeşil
- Kritik migration/DAO/use case/UI akışları testli
- Aktif workout process recreation sonrası korunur
- Signed artifact secret-backed ve imza/profile doğrulamalı
