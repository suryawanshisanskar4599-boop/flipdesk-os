# FlipDesk OS — OPPO Find N3 Flip Cover-Screen Environment

**FlipDesk OS** is a modular Kotlin + Jetpack Compose Android application designed specifically for the **OPPO Find N3 Flip** 3.26-inch vertical AMOLED cover display (`720 × 382` px / `382 × 720` portrait viewport).

## Architecture

```
app/                  -> MainActivity, CoverScreenActivity, Hilt DI, Services, Quick Settings Tile
core/
  design/             -> FLUX Design System (5 themes, glassmorphic surfaces, wallpapers, Palette)
  database/           -> Room DB (widgets, app prefs, theme profiles, notes, reminders) + DataStore + JSON export
  permissions/        -> DeviceCapabilityProbe (ColorOS/Android API & permission diagnostics)
  platform/           -> CoverDisplayManager, InstalledAppRepository, SystemStatusProvider
  animations/         -> FLUX spring physics & haptic feedback
  widgets/            -> Custom Widget Engine (20 built-in widgets + AppWidget compatibility)
features/
  home/               -> CoverHomeScreen (status header, Dynamic Capsule, clocks, multi-page widget grid, gestures)
  launcher/           -> CoverAppDrawer (search, favorites, recents, hidden apps, per-app compatibility status)
  capsule/            -> DynamicCapsuleBar (tap, swipe-dismiss, long-press, RemoteInput inline reply)
  music/              -> MusicSessionController + MiniMusicPlayerCard (MediaSession / Spotify / YT Music)
  quicksettings/      -> FluxControlCentreSheet (Flashlight, DND, Sound, Rotation, Brightness, System Panels)
  customization/      -> CustomizationStudioScreen (live 382x720 preview, presets, sliders, export)
  foldstate/          -> FoldStateTracker (Jetpack WindowManager WindowInfoTracker + DisplayManager)
```

## Build & Run

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

APKs are generated at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`
