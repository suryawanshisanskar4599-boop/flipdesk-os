package com.flipdesk.features.capsule

import android.app.PendingIntent
import android.app.RemoteInput
import android.graphics.Bitmap

enum class CapsuleVisualState {
    IDLE_COMPACT,
    EXPANDED_NOTIFICATION,
    MUSIC_PERSISTENT,
    INCOMING_CALL,
    TIMER_ACTIVE,
    CHARGING_PULSE,
    MESSAGE_REPLY,
    MULTI_STACK
}

enum class CapsuleCategory {
    MESSAGE,
    CALL,
    MISSED_CALL,
    EMAIL,
    CALENDAR,
    MUSIC,
    TIMER,
    ALARM,
    DOWNLOAD,
    CHARGING,
    BATTERY_ALERT,
    NAVIGATION,
    GENERAL
}

data class CapsuleAction(
    val title: String,
    val pendingIntent: PendingIntent?,
    val remoteInput: RemoteInput? = null,
    val isReply: Boolean = false,
    val isMarkRead: Boolean = false
)

data class CapsuleNotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val category: CapsuleCategory,
    val timestamp: Long,
    val iconBitmap: Bitmap? = null,
    val contentIntent: PendingIntent? = null,
    val actions: List<CapsuleAction> = emptyList(),
    val progress: Int? = null,
    val progressMax: Int? = null,
    val isOngoing: Boolean = false
)
