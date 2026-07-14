# GymTracker

GymTracker, antrenman, program, beslenme, olcum ve ilerleme kayitlarini yerelde tutan Android uygulamasidir.

## Gereksinimler

- JDK 17
- Android SDK Platform 34 ve Build Tools
- Android Studio veya Gradle Wrapper

Bu proje Android Gradle Plugin 8.3.1 kullandigi icin JDK 17 ile derlenmelidir. Sistemde farkli bir Java surumu varsayilansa `JAVA_HOME` degerini JDK 17 dizinine yonlendirin.

Windows PowerShell ornegi:

```powershell
$env:JAVA_HOME = "C:\\Program Files\\Java\\jdk-17"
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"
.\\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
export JAVA_HOME="/path/to/jdk-17"
./gradlew assembleDebug
```

## Komutlar

```powershell
# Debug APK uret
.\\gradlew.bat assembleDebug

# Statik analiz
.\\gradlew.bat lintDebug

# Unit testler (test kaynaklari eklendikce calisir)
.\\gradlew.bat testDebugUnitTest

# Room/Android testleri (bagli emulator veya cihaz gerekir)
.\\gradlew.bat connectedDebugAndroidTest
```

Debug APK yolu: `app/build/outputs/apk/debug/app-debug.apk`.

## Gelistirme rehberi

Yeni bir gelistirme oturumunda [Obsidian baslangic notu](docs/obsidian/00_START_HERE.md) ve `AGENTS.md` okunmalidir. Obsidian grafi hizli kesif icindir; guncel kaynak kod ve Git diff'i nihai dogrulama kaynagidir.
