package com.flipdesk.core.database

import com.flipdesk.core.database.dao.WidgetInstanceDao
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import com.flipdesk.core.design.theme.FluxThemeConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SavedLayoutBundle(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val themeConfig: FluxThemeConfig,
    val widgets: List<WidgetInstanceEntity>
)

@Singleton
class LayoutBackupManager @Inject constructor(
    private val widgetDao: WidgetInstanceDao,
    private val preferences: FlipDeskPreferences
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportLayout(currentTheme: FluxThemeConfig): String {
        val widgets = widgetDao.getAll()
        return json.encodeToString(
            SavedLayoutBundle(
                themeConfig = currentTheme,
                widgets = widgets
            )
        )
    }

    suspend fun importLayout(rawJson: String): Result<Int> = runCatching {
        val bundle = json.decodeFromString<SavedLayoutBundle>(rawJson)
        widgetDao.clearAll()
        widgetDao.upsertAll(bundle.widgets)
        preferences.updateThemeConfig(bundle.themeConfig)
        bundle.widgets.size
    }
}
