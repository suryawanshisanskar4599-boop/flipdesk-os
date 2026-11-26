# OPPO Find N3 Flip & ColorOS Compatibility Report

## 1. Hardware & Display Profile
- **Target Device**: OPPO Find N3 Flip (`CPH2519` / `PHT110`)
- **Cover Display**: 3.26-inch AMOLED, `720 × 382` pixels (vertical portrait orientation, 60Hz).
- **Primary Display**: 6.8-inch foldable AMOLED (`Display.DEFAULT_DISPLAY`).

## 2. Capability Matrix (Non-Root)

| Feature | Implementation | Status on ColorOS (Non-Root) | Notes & Fallback |
|---|---|---|---|
| External Cover Display Detection | `DisplayManager.getDisplays()` | **Supported** | Enumerates non-default display IDs & dimensions; falls back to 382×720 viewport preview if no secondary display is active. |
| Fold / Hinge State Detection | `WindowInfoTracker.windowLayoutInfo` + `DisplayManager.DisplayListener` | **Supported** | Detects `FLAT` vs `HALF_OPENED` vs cover-active display state. |
| Automatic Cover-Screen Launch on Fold | `CoverScreenActivity` + `com.oplus.support_sub_display` + `TileService` | **Partial / OS-Gated** | ColorOS restricts background `startActivity` on fold for third-party apps. FlipDesk provides: (1) ColorOS Mini Apps metadata, (2) Quick Settings Tile (`FlipDesk Cover`), (3) optional Accessibility-assisted transition, and (4) automatic unfold restoration (`finish()`). |
| Launch Arbitrary 3rd-Party Apps on Cover | `ActivityOptions.setLaunchDisplayId(coverDisplayId)` | **Restricted by ColorOS** | ColorOS throws `SecurityException` / permission denial for non-system apps targeting protected secondary displays. FlipDesk classifies every app (`Cover Ready`, `Mini App Candidate`, `Main Display Fallback`, `Restricted`) and falls back cleanly to main-display launch. |
| Dynamic Capsule Notifications | `NotificationListenerService` + `RemoteInput` | **Supported** | Requires explicit Notification Access grant. Supports inline reply, mark as read, dismiss, and open app. |
| Music Controls | `MediaSessionManager.getActiveSessions` | **Supported** | Controls Spotify, YouTube Music, and compatible players (requires Notification Access). |
| Quick Settings & Control Centre | `CameraManager.setTorchMode`, `AudioManager`, `NotificationManager`, `Settings.Panel` | **Supported** | Flashlight, Sound Profile, DND, Rotation, and Brightness work directly with user permission; Wi-Fi/Data/Bluetooth/Airplane/Hotspot open official Android Settings Panels instead of faking privileged toggles. |
| Custom Widget Engine | Room-backed Compose Widget Engine | **Supported** | 20 built-in interactive widgets + JSON layout export/import. Standard `AppWidgetHost` binding requires system/launcher host privileges on cover display. |
