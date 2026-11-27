package com.flipdesk.core.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class AppCoverCompatibility(val label: String, val explanation: String) {
    COVER_READY(
        "Cover Ready",
        "Supports resizable / portrait multi-window layout on the 3.26\" cover screen."
    ),
    MINI_APP_ELIGIBLE(
        "Mini App Candidate",
        "Compatible with ColorOS Mini Apps or cover-screen host when enabled in Settings > Foldable features."
    ),
    MAIN_DISPLAY_FALLBACK(
        "Main Display Fallback",
        "ColorOS restricts non-system cover launch for this app; tapping opens on the main display or prompts unfold."
    ),
    SYSTEM_RESTRICTED(
        "Restricted",
        "Sensitive or fixed-landscape app blocked from external display mirroring by OS policy."
    )
}

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val compatibility: AppCoverCompatibility
)

@Singleton
class InstalledAppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coverDisplayManager: CoverDisplayManager
) {
    private val iconCache = LruCache<String, Drawable>(200)
    private val bitmapCache = LruCache<String, Bitmap>(200)

    private val knownMiniAppFriendlyPackages = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.spotify.music",
        "com.google.android.apps.youtube.music",
        "com.google.android.apps.maps",
        "com.google.android.gm",
        "com.google.android.deskclock",
        "com.google.android.calendar",
        "com.android.calculator2",
        "com.coloros.calculator",
        "com.oneplus.calculator"
    )

    suspend fun loadInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val apps = resolveInfos
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                val appInfo = ri.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = ri.loadLabel(pm)?.toString().orEmpty().ifBlank { pkg }
                val compat = evaluateCompatibility(pkg, ri.activityInfo)
                // Pre-warm bitmap cache off the main thread for 60fps drawer scrolling
                if (bitmapCache.get(pkg) == null) {
                    runCatching {
                        val d = ri.loadIcon(pm)
                        iconCache.put(pkg, d)
                        d.toCachedBitmap()?.let { bitmapCache.put(pkg, it) }
                    }
                }
                InstalledAppInfo(
                    packageName = pkg,
                    label = label,
                    isSystemApp = isSystem,
                    compatibility = compat
                )
            }
            .sortedBy { it.label.lowercase() }
        apps
    }

    fun getAppIcon(packageName: String): Drawable? {
        iconCache.get(packageName)?.let { return it }
        return runCatching {
            val icon = context.packageManager.getApplicationIcon(packageName)
            iconCache.put(packageName, icon)
            icon
        }.getOrNull()
    }

    fun getAppBitmap(packageName: String): Bitmap? {
        bitmapCache.get(packageName)?.let { return it }
        val bmp = getAppIcon(packageName)?.toCachedBitmap() ?: return null
        bitmapCache.put(packageName, bmp)
        return bmp
    }

    fun launchApp(packageName: String): CoverDisplayManager.LaunchResult {
        val pm = context.packageManager
        var launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            val queryIntent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(packageName)
            val resolved = pm.queryIntentActivities(queryIntent, 0).firstOrNull()
            if (resolved != null) {
                launchIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(resolved.activityInfo.packageName, resolved.activityInfo.name)
                }
            }
        }
        if (launchIntent == null) {
            return CoverDisplayManager.LaunchResult.FAILED("No launcher activity found for $packageName")
        }
        return coverDisplayManager.launchOnCoverOrFallback(launchIntent, preferCoverDisplay = true)
    }

    private fun evaluateCompatibility(packageName: String, activityInfo: ActivityInfo): AppCoverCompatibility {
        if (packageName in knownMiniAppFriendlyPackages) {
            return AppCoverCompatibility.MINI_APP_ELIGIBLE
        }
        val isLandscapeLocked = activityInfo.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
            activityInfo.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        if (isLandscapeLocked) {
            return AppCoverCompatibility.SYSTEM_RESTRICTED
        }
        val isResizable = activityInfo.applicationInfo.targetSdkVersion >= android.os.Build.VERSION_CODES.N
        return if (isResizable && coverDisplayManager.getCoverDisplay() != null) {
            AppCoverCompatibility.COVER_READY
        } else {
            AppCoverCompatibility.MAIN_DISPLAY_FALLBACK
        }
    }

    private fun Drawable.toCachedBitmap(): Bitmap? = runCatching {
        if (this is BitmapDrawable && bitmap != null) return bitmap
        val w = intrinsicWidth.coerceIn(1, 144)
        val h = intrinsicHeight.coerceIn(1, 144)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bmp
    }.getOrNull()
}
