# FlipDesk OS — Installation & ADB Setup Guide

## 1. Standard Installation (No Root Required)

1. Enable **Developer Options** and **USB Debugging** on your OPPO Find N3 Flip (optional for ADB install, or install the APK directly via file manager).
2. Install the APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   (Or `app-release.apk` for the minified release build.)
3. Open **FlipDesk OS** on the main screen and go to the **Capabilities** tab to grant:
   - **Notification Access** (required for Dynamic Capsule & MediaSession music controls).
   - **Display over other apps (Overlay)** & **Modify System Settings** (for brightness/rotation & cover overlay).
   - **Camera / Microphone** (for quick camera & voice note shortcuts).
   - **Accessibility Service** (optional, for fold-state transition assistance).
4. On ColorOS, go to **Settings → Foldable features → Mini apps** and enable **FlipDesk OS** if listed, or add the **FlipDesk Cover** tile to your Quick Settings panel.

## 2. Optional ADB-Assisted Enhancements

These ADB commands do **not** require root and unlock additional Android multi-display capabilities:

```bash
# Allow FlipDesk to manage secure cover-state settings
adb shell pm grant com.flipdesk.os android.permission.WRITE_SECURE_SETTINGS

# Encourage resizable multi-display activity behavior
adb shell settings put global force_resizable_activities 1
```
*(If using the debug build, replace `com.flipdesk.os` with `com.flipdesk.os.debug`.)*

## 3. Troubleshooting

- **Notifications not appearing in Dynamic Capsule**: Verify Notification Access is enabled in *Settings → Apps → Special app access → Notification access*.
- **Third-party app opens on main screen instead of cover screen**: ColorOS blocks non-system `launchDisplayId` routing for unapproved apps. Enable the app in *Settings → Foldable features → Mini apps* if supported.
- **Brightness/Rotation tile opens Settings instead of toggling**: Grant *Modify system settings* permission when prompted.
