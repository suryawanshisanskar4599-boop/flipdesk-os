package com.flipdesk.core.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import com.flipdesk.core.design.components.FluxGlassCard
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.platform.SystemStatusSnapshot
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WidgetLiveContext(
    val systemStatus: SystemStatusSnapshot = SystemStatusSnapshot(),
    val weatherTempC: Int = 26,
    val weatherCondition: String = "Clear",
    val weatherHiLo: String = "29° / 19°",
    val weatherLocation: String = "Bengaluru",
    val mediaTitle: String = "No Media Playing",
    val mediaArtist: String = "Tap to open player",
    val mediaPlaying: Boolean = false,
    val quickNotePreview: String = "Tap to jot a quick cover note",
    val nextReminder: String = "Review FlipDesk layout"
)

@Composable
fun FluxWidgetCard(
    entity: WidgetInstanceEntity,
    liveContext: WidgetLiveContext,
    isEditMode: Boolean,
    onWidgetClick: () -> Unit,
    onRemove: () -> Unit,
    onCycleSize: () -> Unit,
    onMoveUp: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onMediaPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors
    val rowHeight = if (entity.spanRows >= 2) 156.dp else 78.dp

    FluxGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable { onWidgetClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            when (BuiltInWidgetType.fromId(entity.widgetType)) {
                BuiltInWidgetType.WEATHER -> WeatherWidgetBody(liveContext)
                BuiltInWidgetType.MUSIC_PLAYER -> MusicWidgetBody(
                    liveContext = liveContext,
                    onPlayPause = onMediaPlayPause,
                    onNext = onMediaNext,
                    onPrev = onMediaPrev
                )
                BuiltInWidgetType.BATTERY -> BatteryWidgetBody(liveContext.systemStatus)
                BuiltInWidgetType.STORAGE -> MetricWidgetBody(
                    icon = Icons.Rounded.Storage,
                    title = "Storage",
                    value = String.format(Locale.US, "%.0f GB", liveContext.systemStatus.freeStorageGb),
                    subtitle = "free"
                )
                BuiltInWidgetType.RAM -> MetricWidgetBody(
                    icon = Icons.Rounded.Memory,
                    title = "RAM",
                    value = String.format(Locale.US, "%.1f GB", liveContext.systemStatus.availableRamGb),
                    subtitle = "avail"
                )
                BuiltInWidgetType.WIFI -> MetricWidgetBody(
                    icon = Icons.Rounded.Wifi,
                    title = "Wi-Fi",
                    value = if (liveContext.systemStatus.wifiConnected) "Connected" else if (liveContext.systemStatus.wifiEnabled) "On" else "Off",
                    subtitle = "Tap panel"
                )
                BuiltInWidgetType.BLUETOOTH -> MetricWidgetBody(
                    icon = Icons.Rounded.Bluetooth,
                    title = "Bluetooth",
                    value = if (liveContext.systemStatus.bluetoothEnabled) "Active" else "Off",
                    subtitle = "Devices"
                )
                BuiltInWidgetType.NETWORK -> MetricWidgetBody(
                    icon = Icons.Rounded.Wifi,
                    title = "Network",
                    value = when {
                        liveContext.systemStatus.wifiConnected -> "Wi-Fi"
                        liveContext.systemStatus.cellularConnected -> "5G/LTE"
                        else -> "Offline"
                    },
                    subtitle = "Link"
                )
                BuiltInWidgetType.DATE -> {
                    val day = remember { SimpleDateFormat("EEE", Locale.getDefault()).format(Date()) }
                    val num = remember { SimpleDateFormat("d MMM", Locale.getDefault()).format(Date()) }
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text(day.uppercase(), color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(num, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                BuiltInWidgetType.CLOCK -> {
                    val timeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(timeStr, color = colors.textPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("LOCAL", color = colors.textMuted, fontSize = 10.sp)
                    }
                }
                BuiltInWidgetType.TIMER -> TimerWidgetBody()
                BuiltInWidgetType.STOPWATCH -> StopwatchWidgetBody()
                BuiltInWidgetType.CALCULATOR -> CalculatorWidgetBody()
                BuiltInWidgetType.TASKS -> InfoRowWidgetBody(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Reminders",
                    body = liveContext.nextReminder
                )
                BuiltInWidgetType.NOTES -> InfoRowWidgetBody(
                    icon = Icons.Rounded.CalendarToday,
                    title = "Quick Note",
                    body = liveContext.quickNotePreview
                )
                BuiltInWidgetType.CALENDAR -> InfoRowWidgetBody(
                    icon = Icons.Rounded.CalendarToday,
                    title = "Agenda",
                    body = "No upcoming conflicts"
                )
                BuiltInWidgetType.VOLUME -> MetricWidgetBody(
                    icon = Icons.Rounded.VolumeUp,
                    title = "Volume",
                    value = "Media",
                    subtitle = "Tap to adjust"
                )
                BuiltInWidgetType.NEWS -> InfoRowWidgetBody(
                    icon = Icons.Rounded.WbSunny,
                    title = "Briefing",
                    body = "FlipDesk OS ready on Cover Screen"
                )
                BuiltInWidgetType.CURRENCY -> MetricWidgetBody(
                    icon = Icons.Rounded.Add,
                    title = "USD/INR",
                    value = "84.4",
                    subtitle = "FX"
                )
                BuiltInWidgetType.STOCKS -> InfoRowWidgetBody(
                    icon = Icons.Rounded.Add,
                    title = "Watchlist",
                    body = "GOOG +1.2%  •  AAPL +0.8%"
                )
                null -> InfoRowWidgetBody(
                    icon = Icons.Rounded.Add,
                    title = entity.title,
                    body = if (entity.isAppWidget) "AppWidget #${entity.appWidgetId}" else "Custom Widget"
                )
            }

            if (isEditMode) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .clickable { onMoveUp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SwapVert, contentDescription = "Reorder", tint = colors.textPrimary, modifier = Modifier.size(12.dp))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(alpha = 0.25f))
                            .clickable { onCycleSize() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("${entity.spanColumns}x${entity.spanRows}", color = colors.textPrimary, fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.error.copy(alpha = 0.85f))
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = colors.textPrimary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherWidgetBody(ctx: WidgetLiveContext) {
    val colors = FluxThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(ctx.weatherLocation, color = colors.textSecondary, fontSize = 11.sp)
            Text("${ctx.weatherTempC}°", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = colors.warning, modifier = Modifier.size(20.dp))
            Text("${ctx.weatherCondition} • ${ctx.weatherHiLo}", color = colors.textMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MusicWidgetBody(
    liveContext: WidgetLiveContext,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val colors = FluxThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(liveContext.mediaTitle, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(liveContext.mediaArtist, color = colors.textMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.size(30.dp)) {
                Icon(
                    if (liveContext.mediaPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun BatteryWidgetBody(status: SystemStatusSnapshot) {
    val colors = FluxThemeTokens.colors
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Icon(
                if (status.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                contentDescription = null,
                tint = if (status.isCharging) colors.success else colors.accent,
                modifier = Modifier.size(16.dp)
            )
            Text("${status.batteryPercent}%", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (status.batteryPercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
            color = if (status.batteryPercent <= 20) colors.error else colors.success,
            trackColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun MetricWidgetBody(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    val colors = FluxThemeTokens.colors
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
            Text(title, color = colors.textMuted, fontSize = 10.sp)
        }
        Column {
            Text(value, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = colors.textMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun InfoRowWidgetBody(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    val colors = FluxThemeTokens.colors
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
            Text(title, color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Text(body, color = colors.textPrimary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TimerWidgetBody() {
    val colors = FluxThemeTokens.colors
    var secondsLeft by remember { mutableIntStateOf(300) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
            if (secondsLeft == 0) running = false
        }
    }

    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    Column(
        modifier = Modifier.fillMaxSize().clickable { running = !running },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Timer, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
            Text(if (running) "Running" else "Timer", color = colors.textMuted, fontSize = 10.sp)
        }
        Text(String.format(Locale.US, "%02d:%02d", mins, secs), color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StopwatchWidgetBody() {
    val colors = FluxThemeTokens.colors
    var elapsedSec by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000L)
            elapsedSec++
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().clickable { running = !running },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Stopwatch", color = colors.textMuted, fontSize = 10.sp)
        Text(String.format(Locale.US, "%02d:%02d", elapsedSec / 60, elapsedSec % 60), color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CalculatorWidgetBody() {
    val colors = FluxThemeTokens.colors
    var display by remember { mutableStateOf("0") }
    var pendingOp by remember { mutableStateOf<Char?>(null) }
    var storedValue by remember { mutableStateOf(0.0) }

    fun pressDigit(d: String) {
        display = if (display == "0") d else (display + d).take(10)
    }

    fun applyOp(op: Char) {
        storedValue = display.toDoubleOrNull() ?: 0.0
        pendingOp = op
        display = "0"
    }

    fun evaluate() {
        val current = display.toDoubleOrNull() ?: 0.0
        val res = when (pendingOp) {
            '+' -> storedValue + current
            '-' -> storedValue - current
            '*' -> storedValue * current
            '/' -> if (current != 0.0) storedValue / current else 0.0
            else -> current
        }
        display = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
        pendingOp = null
    }

    val rows = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", "=", "+")
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = display,
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (key in listOf("+", "-", "*", "/", "=")) colors.primary.copy(alpha = 0.25f) else colors.surfaceElevated)
                            .clickable {
                                when (key) {
                                    "C" -> { display = "0"; pendingOp = null; storedValue = 0.0 }
                                    "=" -> evaluate()
                                    "+", "-", "*", "/" -> applyOp(key[0])
                                    else -> pressDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(key, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
