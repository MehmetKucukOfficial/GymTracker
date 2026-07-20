---
type: architecture
status: current
implementation_status: current
verified_commit: 43b523a
updated: 2026-07-20
source_files:
  - app/src/main/java/com/gymtracker/MainActivity.kt
  - app/src/main/java/com/gymtracker/ui/navigation/NavGraph.kt
  - app/src/main/java/com/gymtracker/ui/navigation/DeepLinkContract.kt
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/gymtracker/ui/components/BottomNavigationBar.kt
  - app/src/main/java/com/gymtracker/ui/components/GymTrackerComponents.kt
  - app/src/main/java/com/gymtracker/ui/screens/workout/WorkoutScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/programs/ProgramsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/measurements/MeasurementsScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/progress/ProgressScreen.kt
  - app/src/main/java/com/gymtracker/ui/screens/fatigue/FatigueScreen.kt
  - baselineprofile/src/main/java/com/gymtracker/baselineprofile/ProcessDeathNavigationTest.kt
  - .github/workflows/android-ci.yml
tags: [gymtracker/architecture, gymtracker/navigation]
---

# Mimari — Navigasyon

## Mevcut

Compact genişlikte alt bar; 600 dp ve üzerinde navigation rail kullanılır. Her ikisi de Özet, Antrenman, Programlar, Beslenme, İlerleme olmak üzere aynı 5 hedefi, seçili hedef çözümünü ve save/restore navigasyon sözleşmesini paylaşır. `NavigationPolicy`, nested route’un sahibi olan ana hedefi ve detail ekranlarında ana navigasyon görünürlüğünü tek yerden belirler.

Ölçüm (`progress/measurements`), fotoğraf (`progress/photos`) ve kas yükü (`progress/load`) İlerleme hub’ından açılır ve alt barda İlerleme seçili kalır. Bu üç child rota ortak `GtTopAppBar` geri eylemini gösterir; top bar geri eylemi ve sistem back aynı hub back stack sonucunu üretir. `workouts/{sessionId}` ve `programs/{programId}` detail ekranları alt barı gizler, loading/not-found/content durumlarının tamamında aynı görünür geri sözleşmesini korur; Hilt ViewModel route ID’sini `SavedStateHandle` üzerinden alır ve repository Flow’dan veriyi yeniden çözer.

Workout ve Programs ana rotaları 840 dp+ genişlikte ortak liste–detay iskeletini kullanır. Kart seçimi aynı ana rotada `rememberSaveable` kimliği günceller; önizlemenin açık eylemi mevcut pozitif-ID detay rotasına gider. Böylece compact back davranışı, full-screen detay shell’i, route sahipliği ve repository veri çözümü tek sözleşme olarak kalır.

PR19 dış giriş sözleşmesi `gymtracker://app/<route>` tabanını kullanır. Manifest yalnız `gymtracker` scheme + `app` host eşleşmesini `VIEW/DEFAULT/BROWSABLE` olarak dışa açar; her NavHost destination merkezi `DeepLinkContract` pattern’ine bağlanır. Workout/program detail argümanları `PositiveLongNavType` ile sıfır, negatif ve sayısal olmayan ID’leri route eşleşmesinden çıkarır. Geçersiz veya bilinmeyen URI güvenli `home` başlangıcında kalır.

Gerçek süreç-ölümü kabulü hedef uygulamadan ayrı `:baselineprofile` instrumentation sürecinde çalışır. Test dış URI ile hedef görevi kurar, görevi Home ile arka plana alır, `am kill` sonrasında hedef PID’nin kaybolduğunu kanıtlar ve aynı Android task kimliğini yeniden öne getirir. Yeni PID ile aynı destination metninin görünmesi NavHost/task restorasyonunu; Nutrition tarih metni ile Measurements 90-gün `checked` state’inin korunması da ViewModel `SavedStateHandle` restorasyonunu Activity recreation taklidinden bağımsız doğrular.

PR21 hosted koşusu `29764819516` quality ve API 34 cihaz işlerinde yeşildir; artifact XML 71 app + 2 process-death testini sıfır failure/error/skipped ile doğrular. GitHub PR #15 merge commit `39927a7` ile `master`a alındı ve kaynak dal korundu. PR22 `43b523a`, process-death paketini Nutrition/Measurements state’iyle dört teste genişletir.

## Hedef

Telefon ana hedefleri:

1. Özet
2. Antrenman
3. Programlar
4. Beslenme
5. İlerleme

Ölçüm, fotoğraf ve kas yükü İlerleme altında destination olarak kalır. Aktif workout tam ekran olduğunda ana navigasyon gizlenir. Medium shell’de rail, expanded 840 dp+ Workout/Programs yüzeylerinde kalıcı liste–özet uygulanmıştır.

## Route sözleşmesi

| Route | Parametre | Veri kaynağı |
|---|---|---|
| `home` | yok | Home aggregate Flow |
| `workouts` | yok | Workout repository list Flow |
| `workouts/{sessionId}` | pozitif session ID | Workout repository detail Flow |
| `programs` | yok | Program list Flow |
| `programs/{programId}` | pozitif program ID | Program repository detail Flow |
| `nutrition` | yok | Nutrition ViewModel |
| `progress` | yok | İlerleme hub |
| `progress/measurements` | yok | Measurement Flow |
| `progress/photos` | yok | Photo repository Flow |
| `progress/load` | yok | Fatigue ViewModel |

## Harici URI sözleşmesi

| Ürün hedefi | URI |
|---|---|
| Özet | `gymtracker://app/home` |
| Antrenman listesi/detayı | `gymtracker://app/workouts`, `gymtracker://app/workouts/{positiveId}` |
| Program listesi/detayı | `gymtracker://app/programs`, `gymtracker://app/programs/{positiveId}` |
| Beslenme | `gymtracker://app/nutrition` |
| İlerleme hub/ölçüm/fotoğraf/yük | `gymtracker://app/progress[/measurements|/photos|/load]` |

## Değişiklik kontrol listesi

- [[ADR_002_FIVE_TARGET_NAVIGATION]] kararı
- Ortak top bar back/system back, Activity recreation/selected-tab, compact→rail, 839/840 dp liste–detay, harici cold-start deep link, gerçek OS process-death destination ve SavedState değer testleri geçiyor
- `SavedStateHandle`
- Bottom bar selected state
- Ana destination’larda state restore
- [[FEATURE_PROGRAMS]] satın alma dialog bug’ı
- [[FEATURE_WORKOUT]] detail argümanı
