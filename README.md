# Ruh & Raga Tracker

Aplikasi Android berbasis **Kotlin** dan **Jetpack Compose (Material 3)** untuk melacak rutinitas harian **Ruh (Ibadah)** dan **Raga (Calisthenics & Cardio)** dalam satu antarmuka *dark mode* yang fokus dan responsif.

---

## Fitur Utama

- **Onboarding & Profil Fisik:** Input nama pengguna, usia, berat badan, tinggi badan, serta kalkulasi BMI otomatis yang tersimpan secara lokal (`SharedPreferences`).
- **Today Overview:** Ringkasan progres harian (*Daily Equilibrium* Ruh vs Raga), jadwal shalat berikutnya dengan countdown real-time, akses cepat workout, dan *7-Day Harmony Matrix*.
- **Ibadah Tracker:**
  - Jadwal waktu shalat dinamis (metode **Kemenag RI**, default koordinat Jakarta).
  - Pengingat & notifikasi Android (`AlarmManager` + `NotificationChannel`) dengan opsi suara alarm/adzan.
  - Pencatatan shalat **Jama'ah / Munfarid**, shalat Sunnah, Daily Tilawah, Tasbih Fatimah interaktif, serta **Kompas Arah Kiblat** berbasis sensor perangkat.
- **Workout Logger & Active Fullscreen Timer:** Rep counter (`-1`, `+5`, `+10`, `+25`), daftar set selesai, *Cardio & Metcon Add-on*, serta mode timer layar penuh dengan countdown istirahat.
- **Progress & Consistency Analytics:** Seluruh grafik, persentase konsistensi, *Dual Discipline Matrix* bulanan, *Weekly Dual Balance*, dan *Discipline Milestones* dihitung secara dinamis dari aktivitas yang dicatat pengguna.

---

## 1. Prasyarat & Instalasi Awal

Pastikan perangkat pengembangan sudah memiliki:
- **JDK 17** atau lebih baru
- **Android Studio** (Ladybug / versi terbaru) atau **Android SDK** (`compileSdk 35`, `minSdk 26`, `targetSdk 35`)
- **Android Emulator (AVD)** atau perangkat Android fisik dengan USB Debugging aktif

### Langkah Setup Awal

1. **Clone / buka repository:**
   ```zsh
   cd /Users/macbook/Documents/Coding/android/ruhraga
   ```

2. **Konfigurasi Android SDK (`local.properties`):**
   Buat file `local.properties` di root project (file ini sudah di-ignore oleh `.gitignore`):
   ```properties
   sdk.dir=/Users/<username>/Library/Android/sdk
   ```
   *(Di macOS biasanya `/Users/macbook/Library/Android/sdk`; di Windows `C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk`)*

3. **Pastikan wrapper executable (macOS/Linux):**
   ```zsh
   chmod +x gradlew
   ```

4. **Sinkronisasi & Verifikasi Kompilasi:**
   ```zsh
   ./gradlew compileDebugKotlin --no-daemon
   ```

---

## 2. Cara Development & Menjalankan di Emulator

### Opsi A: Via Android Studio (Disarankan saat Development)
1. Buka **Android Studio** → **Open** → pilih folder `ruhraga`.
2. Tunggu proses **Gradle Sync** selesai.
3. Pilih emulator di dropdown device (misal `Pixel_7_API_30` atau buat baru lewat **Tools → Device Manager**).
4. Klik tombol **Run ▶** (`Ctrl + R` / `Shift + F10`).

### Opsi B: Via Terminal (CLI)

1. **Cek daftar emulator (AVD) yang tersedia:**
   ```zsh
   ~/Library/Android/sdk/emulator/emulator -list-avds
   ```

2. **Jalankan emulator di background** (ganti `Pixel_7_API_30` sesuai nama AVD kamu):
   ```zsh
   ~/Library/Android/sdk/emulator/emulator -avd Pixel_7_API_30 -no-snapshot-load &
   ```

3. **Tunggu hingga emulator selesai booting:**
   ```zsh
   ~/Library/Android/sdk/platform-tools/adb wait-for-device
   while [ "$(~/Library/Android/sdk/platform-tools/adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
     sleep 2
   done
   ```

4. **Build, install, dan jalankan aplikasi ke emulator:**
   ```zsh
   ./gradlew installDebug --no-daemon && ~/Library/Android/sdk/platform-tools/adb shell am start -S -n com.ruhraga.tracker/.MainActivity
   ```

---

## 3. Cara Debug di HP Android (Physical Device)

1. **Aktifkan USB Debugging di HP:**
   - Buka **Settings → About Phone** → ketuk **Build Number** 7x untuk mengaktifkan *Developer Options*.
   - Buka **Settings → Developer Options** → aktifkan **USB Debugging**, lalu sambungkan HP ke Mac via kabel USB.

2. **Cek serial HP yang terhubung:**
   ```zsh
   ~/Library/Android/sdk/platform-tools/adb devices -l
   ```
   Catat serial device HP kamu (contoh: `115333742T002273`).

3. **Build, install, dan jalankan aplikasi langsung di HP:**
   - Jika **hanya HP** yang terhubung (emulator tidak menyala):
     ```zsh
     ./gradlew installDebug --no-daemon && ~/Library/Android/sdk/platform-tools/adb shell am start -S -n com.ruhraga.tracker/.MainActivity
     ```
   - Jika **HP dan emulator sama-sama aktif**, sertakan `-s <SERIAL_HP>`:
     ```zsh
     ./gradlew assembleDebug --no-daemon && ~/Library/Android/sdk/platform-tools/adb -s <SERIAL_HP> install -r --user 0 app/build/outputs/apk/debug/ruhraga.apk && ~/Library/Android/sdk/platform-tools/adb -s <SERIAL_HP> shell am start --user 0 -S -n com.ruhraga.tracker/.MainActivity
     ```

4. **Pantau Logcat (error / debug log) khusus aplikasi di terminal:**
   ```zsh
   ~/Library/Android/sdk/platform-tools/adb -s <SERIAL_HP> logcat --pid=$(~/Library/Android/sdk/platform-tools/adb -s <SERIAL_HP> shell pidof -s com.ruhraga.tracker)
   ```

---

## 4. Perintah Build Menjadi File APK

### Build Debug APK (Untuk Testing / Share Cepat)
```zsh
./gradlew assembleDebug --no-daemon
```
Output APK berada di:
```text
app/build/outputs/apk/debug/ruhraga.apk
```

Untuk meng-install file APK tersebut secara manual ke emulator/HP yang terhubung:
```zsh
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/ruhraga.apk
```

### Build Release APK / App Bundle (Untuk Distribusi)
- **Release APK** (perlu konfigurasi signing keystore di `app/build.gradle.kts` untuk pemasangan di perangkat):
  ```zsh
  ./gradlew assembleRelease --no-daemon
  ```
  Output: `app/build/outputs/apk/release/`

- **Android App Bundle (`.aab` untuk Google Play Store):**
  ```zsh
  ./gradlew bundleRelease --no-daemon
  ```
  Output: `app/build/outputs/bundle/release/app-release.aab`

---

## Struktur Project

```text
app/src/main/java/com/ruhraga/tracker/
├── MainActivity.kt                      # Entry point & request izin notifikasi Android 13+
├── prayer/
│   ├── PrayerTimeCalculator.kt          # Kalkulator jadwal shalat astronomis (Kemenag RI)
│   ├── PrayerAlarmScheduler.kt          # Penjadwalan AlarmManager 5 waktu shalat
│   ├── PrayerAlarmReceiver.kt           # BroadcastReceiver pemicu notifikasi
│   └── PrayerNotificationHelper.kt      # NotificationChannel & builder notifikasi
└── ui/
    ├── components/                      # TopBar, BottomBar, ProgressRings, QiblaCompassSheet
    ├── navigation/RuhRagaNavGraph.kt    # Routing 4 tab + Onboarding + Fullscreen Timer
    ├── screens/                         # Onboarding, Today, Ibadah, Workout, ActiveTimer, Analytics
    ├── state/                           # RuhRagaUiState & RuhRagaViewModel
    └── theme/                           # Color.kt, Type.kt, Theme.kt (Material 3 Dark)
```
