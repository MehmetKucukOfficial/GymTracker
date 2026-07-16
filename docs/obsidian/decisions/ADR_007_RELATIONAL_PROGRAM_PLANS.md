---
type: adr
status: accepted
implementation_status: current
verified_commit: 7917358
updated: 2026-07-15
tags: [gymtracker/decision, gymtracker/data, gymtracker/programs]
---

# ADR 007 — İlişkisel Program Planları ve Planlanan Setler

## Bağlam

Program günleri v6’ya kadar tek JSON string olarak saklanıyor ve Composable içinde parse ediliyordu. Bozuk payload ekranı çökertebiliyor, sorgulanabilir egzersiz kimliği yoktu ve bir program gününü workout’a kopyalamak tamamlanmamış çalışma ile gerçek tamamlanan seti ayıramıyordu.

## Karar

1. Room v7 ile `ProgramDayExercise` ilişkisel tablosu eklenir.
2. Egzersizler v6 isimlerinden locale-independent `exercise.<slug>` kimliğine dönüştürülür; kalıcı ID gösterim adı çevrildiğinde değiştirilmez.
3. `MIGRATION_6_7` geçerli JSON satırlarını ilişkisel modele taşır. Legacy JSON geçiş dönemi boyunca kayıpsız fallback olarak korunur fakat aktif UI/read yolu JSON parse etmez.
4. Program günü tek Room transaction içinde ACTIVE workout’a kopyalanır. Mevcut ACTIVE seans varsa yeni kopya oluşturulmaz.
5. Program setleri `isCompleted=false`, `targetReps`, `exerciseId` ve `exerciseOrder` ile oluşturulur. Gerçek reps/kg kaydedilince tamamlanır; tüm planlı setler çözülmeden seans bitmez.

## Sonuçlar

- UI bozuk JSON parse’ından ayrılır; program egzersizleri sorgulanabilir ve sıralıdır.
- Planı açmak tamamlanmış hacim veya Muscle Load üretmez.
- Migration veri kaybı yerine bozuk payload’ı saklar ve planı güvenli empty/error state’e düşürür.
- `WorkoutSet` hem manuel tamamlanan seti hem programdan gelen planlanan seti temsil eder; ileride ayrı prescription/performance tabloları gerekirse bu v7 sözleşmesi migration kaynağı olur.
- Legacy JSON kolonunun kaldırılması ayrı, kanıtlı bir migration kararıdır.

## Alternatifler

- JSON’u UI’da parse etmeye devam etmek reddedildi: crash, sorgu ve referential-integrity borcu sürer.
- Planı doğrudan tamamlanmış set olarak kopyalamak reddedildi: sahte hacim ve kas yükü üretir.
- Aktif workout üzerine program setlerini eklemek reddedildi: kullanıcı niyeti belirsizdir ve duplicate riski yaratır.

## Doğrulama

[[DatabaseMigrationTest]], [[ProgramSeedInitializerTest]], [[ProgramRepositoryTest]], [[WorkoutRepositoryTest]], [[ProgramsScreenTest]], [[NavigationSmokeTest]].
