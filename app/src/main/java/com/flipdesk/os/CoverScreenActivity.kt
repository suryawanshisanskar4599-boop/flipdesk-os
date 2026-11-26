package com.flipdesk.os

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flipdesk.core.design.theme.FluxTheme
import com.flipdesk.features.foldstate.DevicePosture
import com.flipdesk.features.foldstate.FoldStateTracker
import com.flipdesk.features.home.CoverHomeScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoverScreenActivity : ComponentActivity() {

    private val viewModel: FlipDeskViewModel by viewModels()

    @Inject lateinit var foldStateTracker: FoldStateTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val widgets by viewModel.widgets.collectAsStateWithLifecycle()
            val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
            val notifications by viewModel.notifications.collectAsStateWithLifecycle()
            val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
            val qsState by viewModel.qsState.collectAsStateWithLifecycle()
            val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
            val appPrefs by viewModel.appPreferences.collectAsStateWithLifecycle()

            LaunchedEffect(settings.autoActivateOnFold) {
                foldStateTracker.observePosture(this@CoverScreenActivity).collect { snapshot ->
                    if (settings.autoActivateOnFold &&
                        snapshot.posture == DevicePosture.UNFOLDED_FLAT &&
                        snapshot.coverDisplayId == null
                    ) {
                        // Restore normal main display experience when unfolded
                        finish()
                    }
                }
            }

            FluxTheme(config = settings.themeConfig) {
                CoverHomeScreen(
                    widgets = widgets,
                    systemStatus = systemStatus,
                    notifications = notifications,
                    mediaState = mediaState,
                    qsState = qsState,
                    qsController = viewModel.quickSettingsController,
                    installedApps = installedApps,
                    appPreferences = appPrefs,
                    loadAppIcon = viewModel::loadAppIcon,
                    initialPage = settings.lastCoverPage,
                    gestureSensitivity = settings.gestureSensitivity,
                    capsuleAutoCollapseMs = settings.capsuleAutoCollapseMs,
                    aodMode = settings.aodClockEnabled,
                    onPageChanged = viewModel::updateLastCoverPage,
                    onAddWidget = viewModel::addWidget,
                    onRemoveWidget = viewModel::removeWidget,
                    onResizeWidget = viewModel::resizeWidget,
                    onMoveWidget = viewModel::moveWidget,
                    onLaunchApp = viewModel::launchInstalledApp,
                    onToggleFavoriteApp = viewModel::toggleFavoriteApp,
                    onToggleHiddenApp = viewModel::toggleHiddenApp,
                    onDismissNotification = viewModel::dismissNotification,
                    onTriggerNotificationAction = viewModel::triggerNotificationAction,
                    onOpenNotificationApp = viewModel::openNotificationApp,
                    onMediaPlayPause = viewModel::mediaPlayPause,
                    onMediaNext = viewModel::mediaNext,
                    onMediaPrev = viewModel::mediaPrev,
                    onMediaSeek = viewModel::mediaSeek,
                    onSwitchMediaSession = viewModel::switchMediaSession,
                    onOpenQuickCamera = viewModel::openQuickCamera,
                    onOpenVoiceRecorder = viewModel::openVoiceRecorder,
                    onOpenCustomization = {
                        startActivity(Intent(this@CoverScreenActivity, MainActivity::class.java))
                    }
                )
            }
        }
    }
}
