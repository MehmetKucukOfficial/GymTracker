---
type: adr
status: accepted
implementation_status: current-config
verified_commit: a2586d1
updated: 2026-07-16
source_files:
  - .github/workflows/android-release.yml
  - app/build.gradle.kts
  - .gitignore
tags: [gymtracker/decision, gymtracker/build, gymtracker/security, gymtracker/release]
---

# ADR 009 — Secret-backed production releases

## Bağlam

Normal PR kalite kapısı minified APK/AAB üretir fakat repository production keystore veya parola taşımamalıdır. Signing değerlerinin kısmi tanımlanması sessizce unsigned artifact yayınlamamalı; üretilen dosyanın gerçekten imzalı olduğu build sonrasında doğrulanmalıdır.

## Karar

1. PR/main quality akışı unsigned ve deterministik kalır; production signing yalnız manuel `android-release.yml` workflow’unda çalışır.
2. Keystore base64 biçiminde protected `production` environment secret’ıdır; runner temp alanında `0600` izinle oluşturulur ve `always()` cleanup ile silinir.
3. Store file/password, key alias/password dört atomik değerdir. Biri eksikse `verifyReleaseSigningConfiguration` artifact üretiminden önce fail-fast olur.
4. Release APK `apksigner` ile v2/v3, AAB `jarsigner` ile JAR signature açısından doğrulanmadan artifact upload edilmez.
5. `.jks` ve `.keystore` dosyaları kaynak kontrolü dışında tutulur. Log veya artifact içinde keystore/parola yayımlanmaz.
6. Play App Signing kullanıldığında bu anahtar upload key rolündedir; gerçek Play internal-track yükleme/smoke ayrı operasyonel kapıdır.

## Sonuçlar

- Kod incelemesi production anahtar materyaline erişmeden release sözleşmesini doğrulayabilir.
- Normal CI fork/PR secret erişimi gerektirmez.
- Repository konfigürasyonu hazırdır; gerçek secret tanımı, key rotation ve Play Console yetkisi dış operasyon olarak kalır.

## Kanıt

Geçici self-signed test upload keystore ile `releaseDistribution`; APK v2/v3, AAB JAR signature ve embedded Baseline Profile doğrulamalarını geçti. Secret’sız çağrı dört eksik environment değerini açıkça raporladı. Bağlar: [[ARCH_BUILD_DI]], [[PERFORMANCE_BASELINE]], [[RISK_REGISTER_GRAPH]].
