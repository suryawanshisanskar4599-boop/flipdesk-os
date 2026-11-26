package com.flipdesk.features.home

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.animations.FluxHaptics
import com.flipdesk.core.database.entity.AppPreferenceEntity
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import com.flipdesk.core.design.components.FluxPill
import com.flipdesk.core.design.theme.FluxClockStyle
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.design.wallpaper.FluxWallpaperBackground
import com.flipdesk.core.platform.InstalledAppInfo
import com.flipdesk.core.platform.SystemStatusSnapshot
import com.flipdesk.core.widgets.BuiltInWidgetType
import com.flipdesk.core.widgets.FluxWidgetCard
import com.flipdesk.core.widgets.WidgetLiveContext
import com.flipdesk.core.widgets.WidgetSize
import com.flipdesk.features.capsule.CapsuleAction
import com.flipdesk.features.capsule.CapsuleNotificationItem
import com.flipdesk.features.capsule.DynamicCapsuleBar
import com.flipdesk.features.launcher.CoverAppDrawer
import com.flipdesk.features.music.ActiveMediaState
import com.flipdesk.features.music.MiniMusicPlayerCard
import com.flipdesk.features.quicksettings.FluxControlCentreSheet
import com.flipdesk.features.quicksettings.QuickSettingsController
import com.flipdesk.features.quicksettings.QuickSettingsState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CoverHomeScreen(
    widgets: List<WidgetInstanceEntity>,
    systemStatus: SystemStatusSnapshot,
    notifications: List<CapsuleNotificationItem>,
    mediaState: ActiveMediaState,
    qsState: QuickSettingsState,
    qsController: QuickSettingsController,
    installedApps: List<InstalledAppInfo>,
    appPreferences: Map<String, AppPreferenceEntity>,
    loadAppIcon: (String) -> Drawable?,
    initialPage: Int,
    gestureSensitivity: Float,
    capsuleAutoCollapseMs: Long,
    aodMode: Boolean,
    onPageChanged: (Int) -> Unit,
    onAddWidget: (BuiltInWidgetType, Int) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onResizeWidget: (WidgetInstanceEntity, WidgetSize) -> Unit,
    onMoveWidget: (WidgetInstanceEntity, Int) -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onToggleFavoriteApp: (String) -> Unit,
    onToggleHiddenApp: (String) -> Unit,
    onDismissNotification: (String) -> Unit,
    onTriggerNotificationAction: (CapsuleAction, String?) -> Unit,
    onOpenNotificationApp: (CapsuleNotificationItem) -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onMediaPrev: () -> Unit,
    onMediaSeek: (Long) -> Unit,
    onSwitchMediaSession: () -> Unit,
    onOpenQuickCamera: () -> Unit,
    onOpenVoiceRecorder: () -> Unit,
    onOpenCustomization: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors
    val config = FluxThemeTokens.config
    val view = LocalView.current

    var isEditMode by remember { mutableStateOf(false) }
    var showAppDrawer by remember { mutableStateOf(false) }
    var showControlCentre by remember { mutableStateOf(false) }
    var showMusicSheet by remember { mutableStateOf(false) }
    var showAddWidgetDialog by remember { mutableStateOf(false) }

    val totalPages = ((widgets.maxOfOrNull { it.pageIndex } ?: 0) + 1).coerceAtLeast(3)
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, totalPages - 1)) { totalPages }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(config.showClockSeconds) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(if (config.showClockSeconds) 1000L else 15000L)
        }
    }

    val dragThreshold = (48f / gestureSensitivity.coerceIn(0.5f, 1.5f))

    val liveContext = remember(systemStatus, mediaState) {
        WidgetLiveContext(
            systemStatus = systemStatus,
            mediaTitle = mediaState.title,
            mediaArtist = mediaState.artist,
            mediaPlaying = mediaState.isPlaying
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(dragThreshold) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -dragThreshold) {
                        FluxHaptics.gestureTick(view)
                        showAppDrawer = true
                        showControlCentre = false
                    } else if (dragAmount > dragThreshold) {
                        FluxHaptics.gestureTick(view)
                        showControlCentre = true
                        showAppDrawer = false
                    }
                }
            }
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    FluxHaptics.longPress(view)
                    isEditMode = !isEditMode
                }
            )
    ) {
        if (aodMode) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CoverClockDisplay(nowMillis = nowMillis, style = config.clockStyle, use24h = config.use24HourClock, showSeconds = config.showClockSeconds)
            }
        } else {
            FluxWallpaperBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Header
                CoverStatusHeader(
                    nowMillis = nowMillis,
                    systemStatus = systemStatus,
                    onOpenControlCentre = { showControlCentre = true }
                )

                // Dynamic Capsule
                DynamicCapsuleBar(
                    notifications = notifications,
                    mediaPlaying = mediaState.isPlaying,
                    mediaTitle = mediaState.title,
                    isCharging = systemStatus.isCharging,
                    batteryPercent = systemStatus.batteryPercent,
                    autoCollapseMs = capsuleAutoCollapseMs,
                    onExpandMedia = { showMusicSheet = !showMusicSheet },
                    onDismissItem = onDismissNotification,
                    onTriggerAction = onTriggerNotificationAction,
                    onOpenApp = onOpenNotificationApp
                )

                // Main Clock on Page 0
                if (pagerState.currentPage == 0) {
                    CoverClockDisplay(
                        nowMillis = nowMillis,
                        style = config.clockStyle,
                        use24h = config.use24HourClock,
                        showSeconds = config.showClockSeconds
                    )
                }

                // Edit Mode Toolbar
                if (isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FluxPill(modifier = Modifier.clickable { showAddWidgetDialog = true }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
                                Text("Add Widget", color = colors.textPrimary, fontSize = 11.sp)
                            }
                        }
                        FluxPill(modifier = Modifier.clickable { isEditMode = false }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(14.dp))
                                Text("Done", color = colors.textPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Multi-page Widget Grid
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val pageWidgets = widgets.filter { it.pageIndex == page }.sortedBy { it.orderIndex }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = pageWidgets,
                            key = { it.id },
                            span = { GridItemSpan(it.spanColumns.coerceIn(1, 2)) }
                        ) { widget ->
                            FluxWidgetCard(
                                entity = widget,
                                liveContext = liveContext,
                                isEditMode = isEditMode,
                                onWidgetClick = {
                                    if (widget.widgetType == BuiltInWidgetType.MUSIC_PLAYER.id) {
                                        showMusicSheet = true
                                    }
                                },
                                onRemove = { onRemoveWidget(widget.id) },
                                onCycleSize = {
                                    val sizes = WidgetSize.entries
                                    val current = WidgetSize.fromSpan(widget.spanColumns, widget.spanRows)
                                    val next = sizes[(sizes.indexOf(current) + 1) % sizes.size]
                                    onResizeWidget(widget, next)
                                },
                                onMoveUp = { onMoveWidget(widget, -1) },
                                onMediaPlayPause = onMediaPlayPause,
                                onMediaNext = onMediaNext,
                                onMediaPrev = onMediaPrev
                            )
                        }
                    }
                }

                // Page Dots + One-Handed Navigation Dock
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(totalPages) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == pagerState.currentPage) 14.dp else 5.dp, 5.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == pagerState.currentPage) colors.accent else colors.textMuted.copy(alpha = 0.45f))
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenQuickCamera, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = "Camera", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { showAppDrawer = true }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Rounded.Apps, contentDescription = "Apps", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onOpenVoiceRecorder, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Mic, contentDescription = "Voice Note", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onOpenCustomization, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Tune, contentDescription = "Studio", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(colors.textPrimary.copy(alpha = 0.35f))
                    )
                }
            }
        }

        // Control Centre Overlay
        AnimatedVisibility(
            visible = showControlCentre,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(10.dp)
            ) {
                FluxControlCentreSheet(
                    qsState = qsState,
                    systemStatus = systemStatus,
                    controller = qsController,
                    onClose = { showControlCentre = false }
                )
            }
        }

        // Music Player Sheet
        AnimatedVisibility(
            visible = showMusicSheet,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showMusicSheet = false }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                MiniMusicPlayerCard(
                    media = mediaState,
                    onPlayPause = onMediaPlayPause,
                    onNext = onMediaNext,
                    onPrevious = onMediaPrev,
                    onSeek = onMediaSeek,
                    onSwitchSession = onSwitchMediaSession
                )
            }
        }

        // App Drawer Overlay
        AnimatedVisibility(
            visible = showAppDrawer,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CoverAppDrawer(
                apps = installedApps,
                preferences = appPreferences,
                loadIcon = loadAppIcon,
                onLaunchApp = onLaunchApp,
                onToggleFavorite = onToggleFavoriteApp,
                onToggleHidden = onToggleHiddenApp,
                onClose = { showAppDrawer = false }
            )
        }
    }

    if (showAddWidgetDialog) {
        AlertDialog(
            onDismissRequest = { showAddWidgetDialog = false },
            title = { Text("Add Widget to Page ${pagerState.currentPage + 1}", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BuiltInWidgetType.entries) { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceElevated)
                                .clickable {
                                    onAddWidget(type, pagerState.currentPage)
                                    showAddWidgetDialog = false
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(type.title, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${type.category.label} • ${type.defaultSize.label}", color = colors.textMuted, fontSize = 10.sp)
                            }
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddWidgetDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun CoverStatusHeader(
    nowMillis: Long,
    systemStatus: SystemStatusSnapshot,
    onOpenControlCentre: () -> Unit
) {
    val colors = FluxThemeTokens.colors
    val dateStr = remember(nowMillis / 60000L) {
        SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(nowMillis))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenControlCentre() }
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dateStr, color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (systemStatus.bluetoothEnabled) {
                Icon(Icons.Rounded.Bluetooth, contentDescription = "Bluetooth", tint = colors.accent, modifier = Modifier.size(12.dp))
            }
            if (systemStatus.wifiConnected) {
                Icon(Icons.Rounded.Wifi, contentDescription = "Wi-Fi", tint = colors.textPrimary, modifier = Modifier.size(12.dp))
            } else if (systemStatus.cellularConnected) {
                Icon(Icons.Rounded.SignalCellularAlt, contentDescription = "Cellular", tint = colors.textPrimary, modifier = Modifier.size(12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(
                    if (systemStatus.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                    contentDescription = "Battery",
                    tint = if (systemStatus.isCharging) colors.success else colors.textPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Text("${systemStatus.batteryPercent}%", color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CoverClockDisplay(
    nowMillis: Long,
    style: FluxClockStyle,
    use24h: Boolean,
    showSeconds: Boolean
) {
    val colors = FluxThemeTokens.colors
    val pattern = when {
        use24h && showSeconds -> "HH:mm:ss"
        use24h -> "HH:mm"
        showSeconds -> "h:mm:ss"
        else -> "h:mm"
    }
    val timeText = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(nowMillis))
    val amPm = if (use24h) "" else SimpleDateFormat("a", Locale.getDefault()).format(Date(nowMillis))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val fontFamily = when (style) {
            FluxClockStyle.NOTHING_DOT -> FontFamily.Monospace
            FluxClockStyle.STACKED_EDITORIAL -> FontFamily.Serif
            else -> FontFamily.SansSerif
        }
        val weight = when (style) {
            FluxClockStyle.MINIMAL_THIN -> FontWeight.Light
            FluxClockStyle.MODERN_BOLD -> FontWeight.Bold
            else -> FontWeight.SemiBold
        }
        Text(
            text = timeText,
            color = colors.textPrimary,
            fontSize = if (showSeconds) 34.sp else 40.sp,
            fontWeight = weight,
            fontFamily = fontFamily,
            letterSpacing = (-1.2).sp
        )
        if (amPm.isNotEmpty()) {
            Text(amPm, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}
