package com.flipdesk.features.capsule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.animations.FluxHaptics
import com.flipdesk.core.animations.FluxMotion
import com.flipdesk.core.design.theme.FluxThemeTokens
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DynamicCapsuleBar(
    notifications: List<CapsuleNotificationItem>,
    mediaPlaying: Boolean,
    mediaTitle: String,
    isCharging: Boolean,
    batteryPercent: Int,
    autoCollapseMs: Long,
    onExpandMedia: () -> Unit,
    onDismissItem: (String) -> Unit,
    onTriggerAction: (CapsuleAction, String?) -> Unit,
    onOpenApp: (CapsuleNotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors
    val config = FluxThemeTokens.config
    val view = LocalView.current

    var expanded by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    val topItem = notifications.firstOrNull()

    LaunchedEffect(topItem?.key) {
        if (topItem != null) {
            expanded = true
            delay(autoCollapseMs)
            if (!showReplyInput) expanded = false
        } else {
            expanded = false
        }
    }

    val visualState = when {
        topItem != null && expanded && showReplyInput -> CapsuleVisualState.MESSAGE_REPLY
        topItem != null && expanded -> CapsuleVisualState.EXPANDED_NOTIFICATION
        topItem != null && topItem.category == CapsuleCategory.CALL -> CapsuleVisualState.INCOMING_CALL
        mediaPlaying -> CapsuleVisualState.MUSIC_PERSISTENT
        isCharging -> CapsuleVisualState.CHARGING_PULSE
        notifications.size > 1 -> CapsuleVisualState.MULTI_STACK
        else -> CapsuleVisualState.IDLE_COMPACT
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = FluxMotion.sizeSpring(config.animationIntensity))
            .pointerInput(topItem?.key) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (kotlin.math.abs(dragAmount) > 28f && topItem != null) {
                        FluxHaptics.gestureTick(view)
                        onDismissItem(topItem.key)
                        expanded = false
                        showReplyInput = false
                    }
                }
            }
            .pointerInput(topItem?.key) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 22f && topItem != null) {
                        FluxHaptics.tap(view)
                        expanded = true
                    } else if (dragAmount < -22f) {
                        expanded = false
                        showReplyInput = false
                    }
                }
            }
            .combinedClickable(
                onClick = {
                    FluxHaptics.tap(view)
                    if (topItem != null) {
                        expanded = !expanded
                    } else if (mediaPlaying) {
                        onExpandMedia()
                    }
                },
                onLongClick = {
                    if (topItem != null) {
                        FluxHaptics.longPress(view)
                        expanded = true
                    }
                }
            ),
        shape = RoundedCornerShape(if (expanded) 24.dp else 50.dp),
        color = colors.capsuleBackground,
        border = BorderStroke(0.9.dp, colors.capsuleBorder)
    ) {
        AnimatedContent(targetState = visualState, label = "capsule_state") { state ->
            when (state) {
                CapsuleVisualState.IDLE_COMPACT -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (topItem != null) colors.accent else colors.success)
                            )
                            Text(
                                text = topItem?.title ?: "FlipDesk",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (notifications.size > 1) {
                            Text("+${notifications.size - 1}", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                CapsuleVisualState.MUSIC_PERSISTENT -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = colors.accent, modifier = Modifier.size(15.dp))
                            Text(mediaTitle, color = colors.textPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("Playing", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                CapsuleVisualState.CHARGING_PULSE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = colors.success, modifier = Modifier.size(15.dp))
                            Text("Charging", color = colors.textPrimary, fontSize = 11.sp)
                        }
                        Text("$batteryPercent%", color = colors.success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                CapsuleVisualState.INCOMING_CALL,
                CapsuleVisualState.EXPANDED_NOTIFICATION,
                CapsuleVisualState.MESSAGE_REPLY,
                CapsuleVisualState.TIMER_ACTIVE,
                CapsuleVisualState.MULTI_STACK -> {
                    val item = topItem
                    if (item != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (item.iconBitmap != null) {
                                        Image(
                                            bitmap = item.iconBitmap.asImageBitmap(),
                                            contentDescription = item.appName,
                                            modifier = Modifier.size(22.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            if (item.category == CapsuleCategory.CALL) Icons.Rounded.Call else Icons.Rounded.Notifications,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(item.appName, color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Text(item.title, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { onOpenApp(item) }, modifier = Modifier.size(26.dp)) {
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Open", tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            onDismissItem(item.key)
                                            expanded = false
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = colors.textMuted, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }

                            if (item.text.isNotBlank()) {
                                Text(item.text, color = colors.textSecondary, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }

                            if (item.progress != null && item.progressMax != null && item.progressMax > 0) {
                                LinearProgressIndicator(
                                    progress = { (item.progress.toFloat() / item.progressMax).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)),
                                    color = colors.accent
                                )
                            }

                            if (item.actions.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item.actions.take(3).forEach { action ->
                                        TextButton(
                                            onClick = {
                                                if (action.remoteInput != null) {
                                                    showReplyInput = !showReplyInput
                                                } else {
                                                    onTriggerAction(action, null)
                                                }
                                            }
                                        ) {
                                            Text(action.title, color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            val replyAction = item.actions.firstOrNull { it.remoteInput != null }
                            if (showReplyInput && replyAction != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = replyText,
                                        onValueChange = { replyText = it },
                                        placeholder = { Text("Reply…", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (replyText.isNotBlank()) {
                                                onTriggerAction(replyAction, replyText)
                                                replyText = ""
                                                showReplyInput = false
                                                expanded = false
                                            }
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = colors.accent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
