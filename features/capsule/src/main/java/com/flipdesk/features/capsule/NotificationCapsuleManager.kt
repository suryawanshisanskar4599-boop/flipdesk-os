package com.flipdesk.features.capsule

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCapsuleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _notifications = MutableStateFlow<List<CapsuleNotificationItem>>(emptyList())
    val notifications: StateFlow<List<CapsuleNotificationItem>> = _notifications.asStateFlow()

    private val _activeKey = MutableStateFlow<String?>(null)
    val activeKey: StateFlow<String?> = _activeKey.asStateFlow()

    var onDismissSystemNotification: ((String) -> Unit)? = null

    fun postOrUpdate(item: CapsuleNotificationItem) {
        val current = _notifications.value.toMutableList()
        val idx = current.indexOfFirst { it.key == item.key }
        if (idx >= 0) {
            current[idx] = item
        } else {
            current.add(0, item)
        }
        _notifications.value = current.take(15)
        _activeKey.value = item.key
    }

    fun removeByKey(key: String) {
        val updated = _notifications.value.filterNot { it.key == key }
        _notifications.value = updated
        if (_activeKey.value == key) {
            _activeKey.value = updated.firstOrNull()?.key
        }
    }

    fun clearAll() {
        _notifications.value = emptyList()
        _activeKey.value = null
    }

    fun dismissFromCover(key: String) {
        onDismissSystemNotification?.invoke(key)
        removeByKey(key)
    }

    fun triggerAction(action: CapsuleAction, replyText: String? = null): Boolean {
        val pi = action.pendingIntent ?: return false
        return runCatching {
            val fillIn = Intent()
            if (action.remoteInput != null && !replyText.isNullOrBlank()) {
                val bundle = Bundle().apply {
                    putCharSequence(action.remoteInput.resultKey, replyText)
                }
                RemoteInput.addResultsToIntent(arrayOf(action.remoteInput), fillIn, bundle)
            }
            pi.send(context, 0, fillIn)
            true
        }.getOrDefault(false)
    }

    fun openOriginatingApp(item: CapsuleNotificationItem): Boolean {
        val pi = item.contentIntent
        if (pi != null) {
            return runCatching {
                pi.send()
                true
            }.getOrDefault(false)
        }
        val launch = context.packageManager.getLaunchIntentForPackage(item.packageName) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        return true
    }

    fun classifyPackageAndCategory(packageName: String, androidCategory: String?): CapsuleCategory =
        when {
            androidCategory == android.app.Notification.CATEGORY_CALL -> CapsuleCategory.CALL
            androidCategory == android.app.Notification.CATEGORY_ALARM -> CapsuleCategory.ALARM
            androidCategory == android.app.Notification.CATEGORY_NAVIGATION -> CapsuleCategory.NAVIGATION
            packageName.contains("whatsapp") || packageName.contains("telegram") || packageName.contains("messaging") -> CapsuleCategory.MESSAGE
            packageName.contains("gm") || packageName.contains("mail") -> CapsuleCategory.EMAIL
            packageName.contains("calendar") -> CapsuleCategory.CALENDAR
            packageName.contains("spotify") || packageName.contains("music") -> CapsuleCategory.MUSIC
            packageName.contains("clock") || packageName.contains("deskclock") -> CapsuleCategory.TIMER
            else -> CapsuleCategory.GENERAL
        }
}
