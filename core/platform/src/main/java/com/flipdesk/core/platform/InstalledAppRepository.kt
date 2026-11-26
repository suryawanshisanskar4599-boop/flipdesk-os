package com.flipdesk.core.platform

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
    private val iconCache = LruCache<String, Drawable>(120)

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

        resolveInfos
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                val appInfo = ri.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = ri.loadLabel(pm)?.toString().orEmpty().ifBlank { pkg }
                val compat = evaluateCompatibility(pkg, ri.activityInfo)
                InstalledAppInfo(
                    packageName = pkg,
                    label = label,
                    isSystemApp = isSystem,
                    compatibility = compat
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun getAppIcon(packageName: String): Drawable? {
        iconCache.get(packageName)?.let { return it }
        return runCatching {
            val icon = context.packageManager.getApplicationIcon(packageName)
            iconCache.put(packageName, icon)
            icon
        }.getOrNull()
    }

    fun launchApp(packageName: String): CoverDisplayManager.LaunchResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return CoverDisplayManager.LaunchResult.FAILED("No launcher activity found for $packageName")
        return coverDisplayManager.launchOnCoverOrFallback(launchIntent)
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
}
