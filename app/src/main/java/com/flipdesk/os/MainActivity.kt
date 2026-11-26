package com.flipdesk.os

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flipdesk.core.design.components.FluxGlassCard
import com.flipdesk.core.design.theme.FluxTheme
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.permissions.DeviceCapabilityReport
import com.flipdesk.core.permissions.SupportLevel
import com.flipdesk.features.customization.CustomizationStudioScreen
import com.flipdesk.features.foldstate.DevicePosture
import com.flipdesk.features.foldstate.FoldStateTracker
import com.flipdesk.os.service.CoverOverlayService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: FlipDeskViewModel by viewModels()

    @Inject lateinit var foldStateTracker: FoldStateTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val capabilityReport by viewModel.capabilityReport.collectAsStateWithLifecycle()
            val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val clipboard = LocalClipboardManager.current

            LaunchedEffect(statusBanner) {
                statusBanner?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearBanner()
                }
            }

            LaunchedEffect(settings.autoActivateOnFold) {
                runCatching {
                    foldStateTracker.observePosture(this@MainActivity).collect { snapshot ->
                        if (settings.autoActivateOnFold && snapshot.posture == DevicePosture.FOLDED_COVER_ACTIVE) {
                            viewModel.launchCoverScreen()
                        }
                    }
                }
            }

            FluxTheme(config = settings.themeConfig) {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                                label = { Text("Dashboard") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                                label = { Text("Studio") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2; viewModel.refreshCapabilities() },
                                icon = { Icon(Icons.Rounded.Security, contentDescription = null) },
                                label = { Text("Capabilities") }
                            )
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (selectedTab) {
                            0 -> CompanionDashboardTab(
                                autoActivateOnFold = settings.autoActivateOnFold,
                                aodClockEnabled = settings.aodClockEnabled,
                                onToggleAutoActivate = viewModel::updateAutoActivateOnFold,
                                onToggleAod = viewModel::updateAodClockEnabled,
                                onLaunchCover = viewModel::launchCoverScreen,
                                onStartCoverOverlay = {
                                    if (!Settings.canDrawOverlays(this@MainActivity)) {
                                        startActivity(
                                            Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:$packageName")
                                            )
                                        )
                                    } else {
                                        ContextCompat.startForegroundService(
                                            this@MainActivity,
                                            Intent(this@MainActivity, CoverOverlayService::class.java)
                                        )
                                    }
                                }
                            )
                            1 -> CustomizationStudioScreen(
                                config = settings.themeConfig,
                                gestureSensitivity = settings.gestureSensitivity,
                                capsuleCollapseMs = settings.capsuleAutoCollapseMs,
                                onUpdateConfig = viewModel::updateThemeConfig,
                                onUpdateGestureSensitivity = viewModel::updateGestureSensitivity,
                                onUpdateCapsuleCollapseMs = viewModel::updateCapsuleCollapseMs,
                                onRestoreDefaults = viewModel::restoreDefaults,
                                onExportLayout = {
                                    viewModel.exportLayoutJson { json ->
                                        clipboard.setText(AnnotatedString(json))
                                    }
                                }
                            )
                            2 -> CapabilitiesAndPermissionsTab(
                                report = capabilityReport,
                                onRefresh = viewModel::refreshCapabilities
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionDashboardTab(
    autoActivateOnFold: Boolean,
    aodClockEnabled: Boolean,
    onToggleAutoActivate: (Boolean) -> Unit,
    onToggleAod: (Boolean) -> Unit,
    onLaunchCover: () -> Unit,
    onStartCoverOverlay: () -> Unit
) {
    val colors = FluxThemeTokens.colors
    val context = LocalContext.current
    val overlayGranted = Settings.canDrawOverlays(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("FlipDesk OS", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "FLUX Cover Operating Environment for OPPO Find N3 Flip (3.26\" • 720×382)",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        FluxGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Outer Cover Screen (OPPO Find N3 Flip)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "ColorOS restricts sideloaded apps from the built-in Mini Apps list. Enable Cover Overlay Service to render FlipDesk OS directly on your outer screen when folded.",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Button(onClick = onStartCoverOverlay, modifier = Modifier.fillMaxWidth()) {
                    Text(if (overlayGranted) "Start Cover Screen Overlay Service" else "Grant Overlay Permission for Outer Screen")
                }
                OutlinedButton(onClick = onLaunchCover, modifier = Modifier.fillMaxWidth()) {
                    Text("Preview Cover OS Now")
                }
            }
        }

        FluxGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Activate on Fold", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Switch to Cover OS when folded (where ColorOS permits)", color = colors.textMuted, fontSize = 11.sp)
                    }
                    Switch(checked = autoActivateOnFold, onCheckedChange = onToggleAutoActivate)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AOD Clock Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Pure black Always-On-inspired cover clock", color = colors.textMuted, fontSize = 11.sp)
                    }
                    Switch(checked = aodClockEnabled, onCheckedChange = onToggleAod)
                }
            }
        }
    }
}

@Composable
private fun CapabilitiesAndPermissionsTab(
    report: DeviceCapabilityReport?,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val colors = FluxThemeTokens.colors
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onRefresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Device Capabilities", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onRefresh) { Text("Re-probe") }
        }

        if (report != null) {
            FluxGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${report.manufacturer} ${report.model}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Android ${report.androidVersion} (API ${report.sdkInt}) • ColorOS: ${report.colorOsVersion ?: "Unknown"}", fontSize = 12.sp, color = colors.textSecondary)
                    Text("Displays detected: ${report.displays.size} • Hinge sensor: ${report.hasHingeSensor}", fontSize = 12.sp, color = colors.textMuted)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.weight(1f)
                ) { Text("Notifications", fontSize = 11.sp) }

                OutlinedButton(
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.weight(1f)
                ) { Text("Accessibility", fontSize = 11.sp) }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Overlay", fontSize = 11.sp) }

                OutlinedButton(
                    onClick = {
                        val perms = buildList {
                            add(Manifest.permission.CAMERA)
                            add(Manifest.permission.RECORD_AUDIO)
                            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permLauncher.launch(perms.toTypedArray())
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Camera/Mic", fontSize = 11.sp) }
            }

            report.capabilities.forEach { cap ->
                val badgeColor = when (cap.level) {
                    SupportLevel.SUPPORTED -> colors.success
                    SupportLevel.PARTIAL_FALLBACK -> colors.accent
                    SupportLevel.REQUIRES_PERMISSION -> colors.warning
                    SupportLevel.REQUIRES_ADB_OR_SYSTEM, SupportLevel.UNSUPPORTED -> colors.error
                }
                FluxGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cap.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(cap.level.name, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(cap.detail, fontSize = 11.sp, color = colors.textSecondary)
                        if (cap.adbCommand != null) {
                            Text("ADB: ${cap.adbCommand}", fontSize = 10.sp, color = colors.accent)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
