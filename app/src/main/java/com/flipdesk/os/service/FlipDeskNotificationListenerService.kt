package com.flipdesk.os.service

import android.app.Notification
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.flipdesk.features.capsule.CapsuleAction
import com.flipdesk.features.capsule.CapsuleNotificationItem
import com.flipdesk.features.capsule.NotificationCapsuleManager
import com.flipdesk.features.music.MusicSessionController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlipDeskNotificationListenerService : NotificationListenerService() {

    @Inject lateinit var capsuleManager: NotificationCapsuleManager
    @Inject lateinit var musicController: MusicSessionController

    override fun onListenerConnected() {
        super.onListenerConnected()
        capsuleManager.onDismissSystemNotification = { key ->
            runCatching { cancelNotification(key) }
        }
        runCatching {
            activeNotifications?.forEach { postToCapsule(it) }
            musicController.refreshSessions(ComponentName(this, FlipDeskNotificationListenerService::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName == packageName) return
        postToCapsule(sbn)
        musicController.refreshSessions(ComponentName(this, FlipDeskNotificationListenerService::class.java))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null) {
            capsuleManager.removeByKey(sbn.key)
            musicController.refreshSessions(ComponentName(this, FlipDeskNotificationListenerService::class.java))
        }
    }

    private fun postToCapsule(sbn: StatusBarNotification) {
        val notif = sbn.notification ?: return
        val extras = notif.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val appName = runCatching {
            val ai = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(ai).toString()
        }.getOrDefault(sbn.packageName)

        val actions = notif.actions?.map { action ->
            val remoteInput = action.remoteInputs?.firstOrNull()
            CapsuleAction(
                title = action.title?.toString().orEmpty(),
                pendingIntent = action.actionIntent,
                remoteInput = remoteInput,
                isReply = remoteInput != null,
                isMarkRead = action.title?.toString()?.contains("read", ignoreCase = true) == true
            )
        }.orEmpty()

        val progress = extras.getInt(Notification.EXTRA_PROGRESS, -1).takeIf { it >= 0 }
        val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, -1).takeIf { it > 0 }

        capsuleManager.postOrUpdate(
            CapsuleNotificationItem(
                key = sbn.key,
                packageName = sbn.packageName,
                appName = appName,
                title = title.ifBlank { appName },
                text = text,
                category = capsuleManager.classifyPackageAndCategory(sbn.packageName, notif.category),
                timestamp = sbn.postTime,
                iconBitmap = iconToBitmap(notif.smallIcon),
                contentIntent = notif.contentIntent,
                actions = actions,
                progress = progress,
                progressMax = progressMax,
                isOngoing = sbn.isOngoing
            )
        )
    }

    private fun iconToBitmap(icon: Icon?): Bitmap? = runCatching {
        val drawable = icon?.loadDrawable(this) ?: return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
    }.getOrNull()
}
