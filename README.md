# FOLD CINEMA AI

Aplikasi Android **native** (Kotlin + Jetpack Compose) untuk editor video profesional di **Samsung Galaxy Z Fold6**.

## Status: Tahap 1 — Fondasi

| Fitur | Keterangan |
|---|---|
| Splash screen | Splash sistem (core-splashscreen) + splash Compose beranimasi |
| Antarmuka | Dark cinematic (Material 3, hanya mode gelap) |
| Dashboard | Tombol **New Project**, **Import Video**, **Settings**, daftar proyek terbaru |
| Layout adaptif | Cover screen (< 600dp): satu kolom. Unfolded (≥ 600dp): dua panel |
| Navigasi | Navigation Compose: Splash → Dashboard → New Project / Import Video / Settings / Project |
| Import Video | Pemilih berkas sistem (`OpenDocument`), metadata dibaca via `MediaMetadataRetriever` |
| Penyimpanan | Daftar proyek disimpan di SharedPreferences (JSON); akan diganti Room nanti |

**Belum ada** (sengaja ditunda): AI Auto Edit, color grading OpenGL ES, pemutar Media3, pipeline MediaCodec/MediaExtractor/MediaMuxer, timeline. Halaman proyek hanya placeholder editor.

## Struktur proyek

```
FoldCinemaAI/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── .github/workflows/android-debug.yml     # CI: build APK debug
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                             # tema, warna, ikon adaptif
        └── java/com/foldcinema/ai/
            ├── MainActivity.kt
            ├── navigation/                  # Routes, NavHost
            ├── data/                        # Project, ProjectStore, ProjectViewModel, VideoMetadataReader
            ├── util/                        # Format.kt
            └── ui/
                ├── theme/  adaptive/  components/
                └── splash/ dashboard/ project/ importvideo/ settings/
```

## Versi yang dipakai

Gradle 8.9 · Android Gradle Plugin 8.6.1 · Kotlin 2.0.20 · JDK 17 · compileSdk/targetSdk 35 · minSdk 26 · Compose BOM 2024.09.03 · Navigation Compose 2.8.2.

## Cara build

### Opsi A — GitHub Actions (disarankan)
1. Unggah isi folder ini ke repository GitHub (branch `main` atau `master`).
2. Buka tab **Actions** → workflow **Build Debug APK** (jalan otomatis saat push, atau klik *Run workflow*).
3. Setelah selesai, unduh artifact **FoldCinemaAI-debug-apk** (berisi `app-debug.apk`).

### Opsi B — Android Studio
1. Buka folder proyek dengan Android Studio (Ladybug atau lebih baru) dengan JDK 17.
2. Tunggu Gradle sync, lalu **Build > Build APK(s)**.
3. APK ada di `app/build/outputs/apk/debug/app-debug.apk`.

### Opsi C — Command line
Perlu Gradle 8.9+, JDK 17, dan Android SDK (platform 35):
```
gradle assembleDebug
```
Proyek ini **tidak menyertakan Gradle Wrapper** (`gradle-wrapper.jar` adalah berkas biner). Untuk membuatnya: `gradle wrapper --gradle-version 8.9`, lalu commit `gradlew`, `gradlew.bat`, dan folder `gradle/`.

## Pemasangan
Aktifkan *Install unknown apps* di ponsel, lalu pasang `app-debug.apk`. Application ID build debug: `com.foldcinema.ai.debug`.

## Catatan teknis Fold6
- `MainActivity` menangani `configChanges` (screenSize, screenLayout, dll.), jadi melipat/membuka perangkat **tidak** membuat ulang activity; Compose menyesuaikan layout dari `LocalConfiguration`.
- Breakpoint 600dp / 840dp mengikuti window size class Material. Lebar ±360dp (cover) dan ±670dp (unfolded) adalah perkiraan; verifikasi di perangkat nyata.

## Status verifikasi
Lihat bagian laporan pada pesan penyerahan: build Gradle **belum dijalankan** di lingkungan pembuatan kode ini, jadi APK belum terbukti berhasil dibuat sampai workflow GitHub Actions dijalankan.
