# FOLD CINEMA AI

Aplikasi Android **native** (Kotlin + Jetpack Compose) untuk editor video profesional di **Samsung Galaxy Z Fold6**.

## Status: Tahap 2 — Video Editor Core

| Area | Yang bekerja |
|---|---|
| Import | Satu/banyak video lewat `ACTION_OPEN_DOCUMENT` (`OpenMultipleDocuments`), izin akses dipersistenkan, daftar video terimpor (media library) |
| Player | Media3 ExoPlayer: play/pause, seek bar, waktu saat ini & total, volume, preview video |
| Timeline | Multi-klip, tambah, pilih, hapus, urutkan (tombol Left/Right), durasi tiap klip, playhead |
| Edit non-destruktif | Trim awal/akhir (slider atau "= playhead"), split di playhead, delete, reorder. Hanya rentang waktu yang disimpan; file asli tidak diubah |
| Project | Buat, simpan otomatis tiap edit, buka ulang, hapus. Data Stage 1 dimigrasikan otomatis |
| Layout | Top bar, preview, kontrol, timeline, toolbar. Cover screen: kompak; unfolded: preview/timeline lebih besar |
| Error | Pesan jelas untuk file hilang, izin dicabut, format tak didukung; klip yang tak bisa dibuka ditandai "Missing" |
| Tetap dari Stage 1 | Splash, tema gelap sinematik, dashboard, New Project, Settings, navigasi |

**Belum ada** (sengaja): AI Beat Sync, BPM, auto edit, color grading, 3D LUT, transisi, **export/rendering**, thumbnail frame di timeline, undo/redo, drag-and-drop reorder.

## Cara kerja edit non-destruktif
Tiap klip menyimpan `sourceUri`, `trimStartMs`, `trimEndMs`. Saat diputar, setiap klip menjadi satu `MediaItem` dengan `ClippingConfiguration` (start/end) dalam playlist ExoPlayer. Split membagi satu klip menjadi dua dengan rentang sumber yang berdampingan; trim mengubah rentang; semuanya langsung mengubah playback.

Penyimpanan: `filesDir/projects/<id>.json` per project dan `filesDir/library.json`, ditulis atomik (`AtomicFile`).

## Struktur proyek

```
app/src/main/java/com/foldcinema/ai/
├── MainActivity.kt, FoldCinemaApplication.kt
├── navigation/   Routes, NavHost
├── data/         model, JSON codec, file storage, repository, importer
├── editor/       TimelineOps (logika murni), EditorViewModel
├── playback/     PlaybackEngine (ExoPlayer)
├── util/         Format
└── ui/           theme, adaptive, components, icons,
                  splash, dashboard, project (New Project), importvideo, editor, settings
app/src/test/     TimelineOpsTest (unit test)
```

## Versi yang dipakai

Gradle 8.9 · Android Gradle Plugin 8.6.1 · Kotlin 2.0.20 · JDK 17 · compileSdk/targetSdk 35 · minSdk 26 · Compose BOM 2024.09.03 · Navigation Compose 2.8.2 · Media3 1.4.1.

## Cara build

### Opsi A — GitHub Actions (disarankan)
1. Unggah isi folder ini ke repository GitHub (branch `main`).
2. Buka tab **Actions** → workflow **Build Debug APK** (jalan otomatis saat push, atau klik *Run workflow*).
3. Setelah selesai, unduh artifact **FoldCinemaAI-Debug** (berisi `app-debug.apk`).

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

## Catatan workflow
`.github/workflows/android-debug.yml` sama persis dengan workflow Stage 1 yang sudah terbukti berjalan (JDK 17, Gradle 8.9, Android platform 35 + build-tools 35.0.0). Workflow ini hanya menjalankan `gradle assembleDebug`; unit test (`TimelineOpsTest`) tidak dijalankan otomatis. Jalankan manual dengan `gradle testDebugUnitTest` bila perlu.

## Batasan yang perlu diketahui
- Android membatasi jumlah izin URI persisten (±128 di Android 10 ke bawah, ±512 di Android 11+). Video yang dihapus dari library dilepas izinnya jika tidak dipakai project.
- Setiap edit membangun ulang playlist ExoPlayer (jeda singkat; playback dijeda saat edit).
- Timeline yang sangat panjang diskalakan otomatis agar tetap muat.

## Status verifikasi
Build Gradle **belum dijalankan** di lingkungan pembuatan kode ini (tanpa Gradle, Android SDK, atau jaringan). APK belum terbukti berhasil dibuat sampai workflow GitHub Actions dijalankan. Lihat `CHANGES_STAGE2.md` untuk daftar perubahan.
