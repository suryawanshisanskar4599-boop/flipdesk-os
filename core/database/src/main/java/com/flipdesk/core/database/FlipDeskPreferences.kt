package com.flipdesk.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flipdesk.core.design.theme.FluxThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.flipDeskDataStore: DataStore<Preferences> by preferencesDataStore(name = "flipdesk_prefs")

data class FlipDeskSettings(
    val themeConfig: FluxThemeConfig = FluxThemeConfig(),
    val lastCoverPage: Int = 0,
    val gestureSensitivity: Float = 1.0f,
    val capsuleAutoCollapseMs: Long = 4500L,
    val autoActivateOnFold: Boolean = true,
    val aodClockEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false
)

@Singleton
class FlipDeskPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val THEME_CONFIG_JSON = stringPreferencesKey("theme_config_json")
        val LAST_COVER_PAGE = intPreferencesKey("last_cover_page")
        val GESTURE_SENSITIVITY = floatPreferencesKey("gesture_sensitivity")
        val CAPSULE_AUTO_COLLAPSE_MS = longPreferencesKey("capsule_auto_collapse_ms")
        val AUTO_ACTIVATE_ON_FOLD = booleanPreferencesKey("auto_activate_on_fold")
        val AOD_CLOCK_ENABLED = booleanPreferencesKey("aod_clock_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<FlipDeskSettings> = context.flipDeskDataStore.data.map { prefs ->
        val config = prefs[Keys.THEME_CONFIG_JSON]?.let { raw ->
            runCatching { json.decodeFromString<FluxThemeConfig>(raw) }.getOrNull()
        } ?: FluxThemeConfig()

        FlipDeskSettings(
            themeConfig = config,
            lastCoverPage = prefs[Keys.LAST_COVER_PAGE] ?: 0,
            gestureSensitivity = prefs[Keys.GESTURE_SENSITIVITY] ?: 1.0f,
            capsuleAutoCollapseMs = prefs[Keys.CAPSULE_AUTO_COLLAPSE_MS] ?: 4500L,
            autoActivateOnFold = prefs[Keys.AUTO_ACTIVATE_ON_FOLD] ?: true,
            aodClockEnabled = prefs[Keys.AOD_CLOCK_ENABLED] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateThemeConfig(config: FluxThemeConfig) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.THEME_CONFIG_JSON] = json.encodeToString(config)
        }
    }

    suspend fun updateLastCoverPage(page: Int) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.LAST_COVER_PAGE] = page
        }
    }

    suspend fun updateGestureSensitivity(sensitivity: Float) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.GESTURE_SENSITIVITY] = sensitivity.coerceIn(0.5f, 1.5f)
        }
    }

    suspend fun updateCapsuleAutoCollapseMs(ms: Long) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.CAPSULE_AUTO_COLLAPSE_MS] = ms.coerceIn(2000L, 12000L)
        }
    }

    suspend fun updateAutoActivateOnFold(enabled: Boolean) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.AUTO_ACTIVATE_ON_FOLD] = enabled
        }
    }

    suspend fun updateAodClockEnabled(enabled: Boolean) {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.AOD_CLOCK_ENABLED] = enabled
        }
    }

    suspend fun markOnboardingCompleted() {
        context.flipDeskDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
