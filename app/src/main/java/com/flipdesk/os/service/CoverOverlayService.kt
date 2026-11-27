package com.flipdesk.os.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.flipdesk.core.database.FlipDeskPreferences
import com.flipdesk.core.database.FlipDeskSettings
import com.flipdesk.core.database.dao.AppPreferenceDao
import com.flipdesk.core.design.theme.FluxTheme
import com.flipdesk.core.platform.CoverDisplayManager
import com.flipdesk.core.platform.InstalledAppRepository
import com.flipdesk.core.platform.SystemStatusProvider
import com.flipdesk.core.platform.SystemStatusSnapshot
import com.flipdesk.core.widgets.WidgetEngineRepository
import com.flipdesk.features.capsule.NotificationCapsuleManager
import com.flipdesk.features.home.CoverHomeScreen
import com.flipdesk.features.music.MusicSessionController
import com.flipdesk.features.quicksettings.QuickSettingsController
import com.flipdesk.os.MainActivity
import com.flipdesk.os.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Renders FlipDesk Cover OS directly onto the OPPO Find N3 Flip's external display
 * using DisplayContext + WindowManager TYPE_APPLICATION_OVERLAY when folded,
 * bypassing ColorOS's Mini Apps whitelist restriction without root.
 */
@AndroidEntryPoint
class CoverOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    @Inject lateinit var preferences: FlipDeskPreferences
    @Inject lateinit var widgetRepository: WidgetEngineRepository
    @Inject lateinit var appRepository: InstalledAppRepository
    @Inject lateinit var appPreferenceDao: AppPreferenceDao
    @Inject lateinit var systemStatusProvider: SystemStatusProvider
    @Inject lateinit var capsuleManager: NotificationCapsuleManager
    @Inject lateinit var musicController: MusicSessionController
    @Inject lateinit var quickSettingsController: QuickSettingsController
    @Inject lateinit var coverDisplayManager: CoverDisplayManager

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private lateinit var displayManager: DisplayManager
    private var overlayView: ComposeView? = null
    private var overlayWindowManager: WindowManager? = null
    private var activeCoverDisplayId: Int? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = evaluateDisplays()
        override fun onDisplayRemoved(displayId: Int) = evaluateDisplays()
        override fun onDisplayChanged(displayId: Int) = evaluateDisplays()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        startForegroundNotification()
        displayManager.registerDisplayListener(displayListener, null)
        evaluateDisplays()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        evaluateDisplays()
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlay()
        displayManager.unregisterDisplayListener(displayListener)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun evaluateDisplays() {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlay()
            return
        }
        val coverDisplay = displayManager.displays.firstOrNull {
            it.displayId != Display.DEFAULT_DISPLAY && it.state == Display.STATE_ON
        }
        if (coverDisplay != null) {
            if (activeCoverDisplayId != coverDisplay.displayId || overlayView == null) {
                showOverlayOnDisplay(coverDisplay)
            }
        } else {
            removeOverlay()
        }
    }

    private fun showOverlayOnDisplay(display: Display) {
        removeOverlay()
        runCatching {
            val displayContext = createDisplayContext(display)
            val wm = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val settingsFlow = preferences.settings.stateIn(serviceScope, SharingStarted.Eagerly, FlipDeskSettings())
            val widgetsFlow = widgetRepository.observeWidgets().stateIn(serviceScope, SharingStarted.Eagerly, emptyList())
            val statusFlow = systemStatusProvider.observeSystemStatus().stateIn(serviceScope, SharingStarted.Eagerly, systemStatusProvider.currentSnapshot())
            val prefsFlow = appPreferenceDao.observeAll().map { it.associateBy { p -> p.packageName } }
                .stateIn(serviceScope, SharingStarted.Eagerly, emptyMap())

            val appsState = kotlinx.coroutines.flow.MutableStateFlow(emptyList<com.flipdesk.core.platform.InstalledAppInfo>())
            serviceScope.launch {
                appsState.value = appRepository.loadInstalledApps()
            }

            val view = ComposeView(displayContext).apply {
                setViewTreeLifecycleOwner(this@CoverOverlayService)
                setViewTreeSavedStateRegistryOwner(this@CoverOverlayService)
                setViewTreeViewModelStoreOwner(this@CoverOverlayService)
                setContent {
                    val settings by settingsFlow.collectAsStateWithLifecycle()
                    val widgets by widgetsFlow.collectAsStateWithLifecycle()
                    val status by statusFlow.collectAsStateWithLifecycle()
                    val notifications by capsuleManager.notifications.collectAsStateWithLifecycle()
                    val media by musicController.state.collectAsStateWithLifecycle()
                    val qs by quickSettingsController.state.collectAsStateWithLifecycle()
                    val apps by appsState.collectAsStateWithLifecycle()
                    val appPrefs by prefsFlow.collectAsStateWithLifecycle()

                    FluxTheme(config = settings.themeConfig) {
                        CoverHomeScreen(
                            widgets = widgets,
                            systemStatus = status,
                            notifications = notifications,
                            mediaState = media,
                            qsState = qs,
                            qsController = quickSettingsController,
                            installedApps = apps,
                            appPreferences = appPrefs,
                            loadAppIcon = appRepository::getAppIcon,
                            initialPage = settings.lastCoverPage,
                            gestureSensitivity = settings.gestureSensitivity,
                            capsuleAutoCollapseMs = settings.capsuleAutoCollapseMs,
                            aodMode = settings.aodClockEnabled,
                            onPageChanged = { page -> serviceScope.launch { preferences.updateLastCoverPage(page) } },
                            onAddWidget = { type, page -> serviceScope.launch { widgetRepository.addBuiltInWidget(type, page) } },
                            onRemoveWidget = { id -> serviceScope.launch { widgetRepository.removeWidget(id) } },
                            onResizeWidget = { e, s -> serviceScope.launch { widgetRepository.resizeWidget(e, s) } },
                            onMoveWidget = { e, d -> serviceScope.launch { widgetRepository.moveWidget(e, d) } },
                            onLaunchApp = { app ->
                                removeOverlay()
                                appRepository.launchApp(app.packageName)
                            },
                            onToggleFavoriteApp = { pkg ->
                                serviceScope.launch {
                                    val cur = appPrefs[pkg] ?: com.flipdesk.core.database.entity.AppPreferenceEntity(pkg)
                                    appPreferenceDao.upsert(cur.copy(isFavorite = !cur.isFavorite))
                                }
                            },
                            onToggleHiddenApp = { pkg ->
                                serviceScope.launch {
                                    val cur = appPrefs[pkg] ?: com.flipdesk.core.database.entity.AppPreferenceEntity(pkg)
                                    appPreferenceDao.upsert(cur.copy(isHidden = !cur.isHidden))
                                }
                            },
                            onDismissNotification = capsuleManager::dismissFromCover,
                            onTriggerNotificationAction = capsuleManager::triggerAction,
                            onOpenNotificationApp = capsuleManager::openOriginatingApp,
                            onMediaPlayPause = musicController::togglePlayPause,
                            onMediaNext = musicController::skipNext,
                            onMediaPrev = musicController::skipPrevious,
                            onMediaSeek = musicController::seekTo,
                            onSwitchMediaSession = musicController::cycleSession,
                            onOpenQuickCamera = {
                                removeOverlay()
                                val i = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                                coverDisplayManager.launchOnCoverOrFallback(i)
                            },
                            onOpenVoiceRecorder = {
                                removeOverlay()
                                val i = Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                                coverDisplayManager.launchOnCoverOrFallback(i)
                            },
                            onOpenCustomization = {
                                removeOverlay()
                                startActivity(Intent(this@CoverOverlayService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        )
                    }
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
            )

            wm.addView(view, params)
            overlayView = view
            overlayWindowManager = wm
            activeCoverDisplayId = display.displayId
        }
    }

    private fun removeOverlay() {
        val view = overlayView
        val wm = overlayWindowManager
        if (view != null && wm != null) {
            runCatching { wm.removeViewImmediate(view) }
        }
        overlayView = null
        overlayWindowManager = null
        activeCoverDisplayId = null
    }

    private fun startForegroundNotification() {
        val channelId = "flipdesk_cover_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(channelId, "FlipDesk Cover OS", NotificationManager.IMPORTANCE_MIN)
            )
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_flipdesk_tile)
            .setContentTitle("FlipDesk Cover OS Active")
            .setContentText("Ready when phone is folded")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
        startForeground(101, notif)
    }
}
