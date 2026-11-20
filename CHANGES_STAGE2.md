# Perubahan Stage 1 → Stage 2

## Dihapus (digantikan)
- `data/ProjectStore.kt` (SharedPreferences) → diganti `ProjectStorage` (file JSON). Data Stage 1 dimigrasikan otomatis saat pertama dibuka.
- `data/ProjectViewModel.kt` → diganti `ProjectRepository`.
- `ui/project/ProjectScreen.kt` (placeholder) → diganti editor sungguhan.

## Baru
- `FoldCinemaApplication.kt` — memegang `ProjectRepository`.
- `data/`: `ProjectCodec`, `ProjectStorage`, `ProjectRepository`, `MediaImporter`, `UriAccess`.
- `editor/`: `TimelineOps` (logika split/trim/move/delete, murni Kotlin), `EditorViewModel`.
- `playback/PlaybackEngine.kt` — Media3 ExoPlayer, satu `MediaItem` + `ClippingConfiguration` per klip.
- `ui/editor/`: `EditorScreen` (+`EditorRoute`), `PlaybackControls`, `TimelineView`, `TrimPanel`, `EditorToolbar`, `AddClipDialog`.
- `ui/icons/EditorIcons.kt`, `res/layout/view_player.xml`.
- `app/src/test/.../TimelineOpsTest.kt` — unit test logika edit.

## Diubah
- `data/Project.kt` — model: `Clip`, `Project` (daftar klip), `MediaAsset`.
- `ui/importvideo/ImportVideoScreen.kt` — multi-import + daftar video terimpor + buat project dari pilihan.
- `ui/components/Components.kt` — `CinemaTopBar` (slot aksi), `ProjectCard` (jumlah klip/durasi), `AssetRow`, `Banner`.
- `navigation/FoldCinemaApp.kt`, `ui/settings/SettingsScreen.kt`, `util/Format.kt`.
- `app/build.gradle.kts` (media3 1.4.1, lifecycle-runtime-compose, junit; versi 0.2.0), `AndroidManifest.xml` (Application class).
- `README.md`.

## Tidak berubah
`.github/workflows/android-debug.yml` (identik dengan workflow Stage 1 milik Anda), Splash, tema, dashboard, New Project, Settings (selain teks status), `VideoMetadataReader`, `MainActivity`, ikon.
