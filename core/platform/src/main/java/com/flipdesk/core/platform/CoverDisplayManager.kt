package com.flipdesk.core.platform

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class CoverDisplayInfo(
    val displayId: Int,
    val widthPx: Int,
    val heightPx: Int,
    val isExternalDisplay: Boolean,
    val state: Int
)

@Singleton
class CoverDisplayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    fun getCoverDisplay(): CoverDisplayInfo? {
        val secondary = displayManager.displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
            ?: return null
        val size = Point()
        @Suppress("DEPRECATION")
        secondary.getRealSize(size)
        return CoverDisplayInfo(
            displayId = secondary.displayId,
            widthPx = size.x,
            heightPx = size.y,
            isExternalDisplay = true,
            state = secondary.state
        )
    }

    fun observeDisplayChanges(): Flow<CoverDisplayInfo?> = callbackFlow {
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) { trySend(getCoverDisplay()) }
            override fun onDisplayRemoved(displayId: Int) { trySend(getCoverDisplay()) }
            override fun onDisplayChanged(displayId: Int) { trySend(getCoverDisplay()) }
        }
        trySend(getCoverDisplay())
        displayManager.registerDisplayListener(listener, null)
        awaitClose { displayManager.unregisterDisplayListener(listener) }
    }

    /**
     * Attempts to launch [intent] on the secondary cover display if [preferCoverDisplay] is true.
     * Falls back cleanly to the default display so apps never get stuck or silently dropped.
     */
    fun launchOnCoverOrFallback(intent: Intent, preferCoverDisplay: Boolean = true): LaunchResult {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val cover = if (preferCoverDisplay) getCoverDisplay() else null
        if (cover != null && cover.state == Display.STATE_ON) {
            try {
                val options = ActivityOptions.makeBasic().apply {
                    launchDisplayId = cover.displayId
                }
                context.startActivity(intent, options.toBundle())
                return LaunchResult.LAUNCHED_ON_COVER
            } catch (_: Throwable) {
                // Fall back to default display below
            }
        }
        return runCatching {
            val defaultOpts = ActivityOptions.makeBasic().apply {
                launchDisplayId = Display.DEFAULT_DISPLAY
            }
            context.startActivity(intent, defaultOpts.toBundle())
            LaunchResult.LAUNCHED_ON_MAIN_FALLBACK
        }.recoverCatching {
            context.startActivity(intent)
            LaunchResult.LAUNCHED_ON_MAIN_FALLBACK
        }.getOrElse {
            LaunchResult.FAILED(it.message ?: "Unable to launch activity")
        }
    }

    sealed class LaunchResult {
        data object LAUNCHED_ON_COVER : LaunchResult()
        data object LAUNCHED_ON_MAIN_FALLBACK : LaunchResult()
        data class FAILED(val reason: String) : LaunchResult()
    }
}
