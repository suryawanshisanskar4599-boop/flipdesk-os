package com.flipdesk.os

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipdesk.core.database.FlipDeskPreferences
import com.flipdesk.core.database.FlipDeskSettings
import com.flipdesk.core.database.LayoutBackupManager
import com.flipdesk.core.database.dao.AppPreferenceDao
import com.flipdesk.core.database.entity.AppPreferenceEntity
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import com.flipdesk.core.design.theme.FluxThemeConfig
import com.flipdesk.core.permissions.DeviceCapabilityProbe
import com.flipdesk.core.permissions.DeviceCapabilityReport
import com.flipdesk.core.platform.CoverDisplayManager
import com.flipdesk.core.platform.InstalledAppInfo
import com.flipdesk.core.platform.InstalledAppRepository
import com.flipdesk.core.platform.SystemStatusProvider
import com.flipdesk.core.platform.SystemStatusSnapshot
import com.flipdesk.core.widgets.BuiltInWidgetType
import com.flipdesk.core.widgets.WidgetEngineRepository
import com.flipdesk.core.widgets.WidgetSize
import com.flipdesk.features.capsule.CapsuleAction
import com.flipdesk.features.capsule.CapsuleNotificationItem
import com.flipdesk.features.capsule.NotificationCapsuleManager
import com.flipdesk.features.music.ActiveMediaState
import com.flipdesk.features.music.MusicSessionController
import com.flipdesk.features.quicksettings.QuickSettingsController
import com.flipdesk.features.quicksettings.QuickSettingsState
import com.flipdesk.os.service.FlipDeskAccessibilityService
import com.flipdesk.os.service.FlipDeskNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlipDeskViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: FlipDeskPreferences,
    private val widgetRepository: WidgetEngineRepository,
    private val appRepository: InstalledAppRepository,
    private val appPreferenceDao: AppPreferenceDao,
    private val systemStatusProvider: SystemStatusProvider,
    private val capsuleManager: NotificationCapsuleManager,
    private val musicController: MusicSessionController,
    val quickSettingsController: QuickSettingsController,
    private val coverDisplayManager: CoverDisplayManager,
    private val capabilityProbe: DeviceCapabilityProbe,
    private val layoutBackupManager: LayoutBackupManager
) : ViewModel() {

    val settings: StateFlow<FlipDeskSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FlipDeskSettings())

    val widgets: StateFlow<List<WidgetInstanceEntity>> = widgetRepository.observeWidgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemStatus: StateFlow<SystemStatusSnapshot> = systemStatusProvider.observeSystemStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), systemStatusProvider.currentSnapshot())

    val notifications: StateFlow<List<CapsuleNotificationItem>> = capsuleManager.notifications
    val mediaState: StateFlow<ActiveMediaState> = musicController.state
    val qsState: StateFlow<QuickSettingsState> = quickSettingsController.state

    val appPreferences: StateFlow<Map<String, AppPreferenceEntity>> = appPreferenceDao.observeAll()
        .map { list -> list.associateBy { it.packageName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _capabilityReport = MutableStateFlow<DeviceCapabilityReport?>(null)
    val capabilityReport: StateFlow<DeviceCapabilityReport?> = _capabilityReport.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        refreshApps()
        refreshCapabilities()
        runCatching { refreshMedia() }
    }

    fun refreshApps() {
        viewModelScope.launch {
            runCatching {
                _installedApps.value = appRepository.loadInstalledApps()
            }
        }
    }

    fun refreshCapabilities() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                _capabilityReport.value = capabilityProbe.probe(
                    notificationListenerComponent = ComponentName(context, FlipDeskNotificationListenerService::class.java),
                    accessibilityComponent = ComponentName(context, FlipDeskAccessibilityService::class.java)
                )
            }
        }
    }

    fun refreshMedia() {
        musicController.refreshSessions(
            ComponentName(context, FlipDeskNotificationListenerService::class.java)
        )
    }

    fun loadAppIcon(packageName: String): Drawable? = appRepository.getAppIcon(packageName)

    fun launchCoverScreen() {
        val intent = Intent(context, CoverScreenActivity::class.java)
        val result = coverDisplayManager.launchOnCoverOrFallback(intent)
        if (result is CoverDisplayManager.LaunchResult.FAILED) {
            _statusBanner.value = result.reason
        }
    }

    fun launchInstalledApp(app: InstalledAppInfo) {
        viewModelScope.launch {
            val existing = appPreferences.value[app.packageName] ?: AppPreferenceEntity(app.packageName)
            appPreferenceDao.upsert(existing.copy(lastLaunchedAt = System.currentTimeMillis()))
        }
        val res = appRepository.launchApp(app.packageName)
        if (res is CoverDisplayManager.LaunchResult.LAUNCHED_ON_MAIN_FALLBACK) {
            _statusBanner.value = "${app.label}: ${app.compatibility.label}"
        } else if (res is CoverDisplayManager.LaunchResult.FAILED) {
            _statusBanner.value = res.reason
        }
    }

    fun toggleFavoriteApp(packageName: String) {
        viewModelScope.launch {
            val existing = appPreferences.value[packageName] ?: AppPreferenceEntity(packageName)
            appPreferenceDao.upsert(existing.copy(isFavorite = !existing.isFavorite))
        }
    }

    fun toggleHiddenApp(packageName: String) {
        viewModelScope.launch {
            val existing = appPreferences.value[packageName] ?: AppPreferenceEntity(packageName)
            appPreferenceDao.upsert(existing.copy(isHidden = !existing.isHidden))
        }
    }

    fun updateThemeConfig(config: FluxThemeConfig) {
        viewModelScope.launch { preferences.updateThemeConfig(config) }
    }

    fun updateLastCoverPage(page: Int) {
        viewModelScope.launch { preferences.updateLastCoverPage(page) }
    }

    fun updateGestureSensitivity(sensitivity: Float) {
        viewModelScope.launch { preferences.updateGestureSensitivity(sensitivity) }
    }

    fun updateCapsuleCollapseMs(ms: Long) {
        viewModelScope.launch { preferences.updateCapsuleAutoCollapseMs(ms) }
    }

    fun updateAutoActivateOnFold(enabled: Boolean) {
        viewModelScope.launch { preferences.updateAutoActivateOnFold(enabled) }
    }

    fun updateAodClockEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.updateAodClockEnabled(enabled) }
    }

    fun addWidget(type: BuiltInWidgetType, pageIndex: Int) {
        viewModelScope.launch { widgetRepository.addBuiltInWidget(type, pageIndex) }
    }

    fun removeWidget(id: String) {
        viewModelScope.launch { widgetRepository.removeWidget(id) }
    }

    fun resizeWidget(entity: WidgetInstanceEntity, newSize: WidgetSize) {
        viewModelScope.launch { widgetRepository.resizeWidget(entity, newSize) }
    }

    fun moveWidget(entity: WidgetInstanceEntity, direction: Int) {
        viewModelScope.launch { widgetRepository.moveWidget(entity, direction) }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            preferences.updateThemeConfig(FluxThemeConfig())
            widgetRepository.resetToDefaults()
            _statusBanner.value = "Restored default FLUX theme & widgets"
        }
    }

    fun exportLayoutJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = layoutBackupManager.exportLayout(settings.value.themeConfig)
            onResult(json)
        }
    }

    fun dismissNotification(key: String) = capsuleManager.dismissFromCover(key)

    fun triggerNotificationAction(action: CapsuleAction, reply: String?) {
        capsuleManager.triggerAction(action, reply)
    }

    fun openNotificationApp(item: CapsuleNotificationItem) {
        capsuleManager.openOriginatingApp(item)
    }

    fun mediaPlayPause() = musicController.togglePlayPause()
    fun mediaNext() = musicController.skipNext()
    fun mediaPrev() = musicController.skipPrevious()
    fun mediaSeek(pos: Long) = musicController.seekTo(pos)
    fun switchMediaSession() = musicController.cycleSession()

    fun openQuickCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        coverDisplayManager.launchOnCoverOrFallback(intent)
    }

    fun openVoiceRecorder() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        coverDisplayManager.launchOnCoverOrFallback(intent)
    }

    fun clearBanner() {
        _statusBanner.value = null
    }
}
