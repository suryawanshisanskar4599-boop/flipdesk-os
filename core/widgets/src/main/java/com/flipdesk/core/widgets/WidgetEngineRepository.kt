package com.flipdesk.core.widgets

import com.flipdesk.core.database.dao.WidgetInstanceDao
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetEngineRepository @Inject constructor(
    private val widgetDao: WidgetInstanceDao
) {
    private var seeded = false

    fun observeWidgets(): Flow<List<WidgetInstanceEntity>> =
        widgetDao.observeAll().onEach { list ->
            if (list.isEmpty() && !seeded) {
                seeded = true
                seedDefaultWidgets()
            }
        }

    suspend fun seedDefaultWidgets() {
        val defaults = listOf(
            WidgetInstanceEntity(
                id = "w_weather_0",
                pageIndex = 0,
                widgetType = BuiltInWidgetType.WEATHER.id,
                title = "Weather",
                spanColumns = 2,
                spanRows = 1,
                orderIndex = 0
            ),
            WidgetInstanceEntity(
                id = "w_music_0",
                pageIndex = 0,
                widgetType = BuiltInWidgetType.MUSIC_PLAYER.id,
                title = "Now Playing",
                spanColumns = 2,
                spanRows = 1,
                orderIndex = 1
            ),
            WidgetInstanceEntity(
                id = "w_battery_0",
                pageIndex = 0,
                widgetType = BuiltInWidgetType.BATTERY.id,
                title = "Battery",
                spanColumns = 1,
                spanRows = 1,
                orderIndex = 2
            ),
            WidgetInstanceEntity(
                id = "w_timer_0",
                pageIndex = 0,
                widgetType = BuiltInWidgetType.TIMER.id,
                title = "Timer",
                spanColumns = 1,
                spanRows = 1,
                orderIndex = 3
            ),
            WidgetInstanceEntity(
                id = "w_tasks_1",
                pageIndex = 1,
                widgetType = BuiltInWidgetType.TASKS.id,
                title = "Reminders",
                spanColumns = 2,
                spanRows = 1,
                orderIndex = 0
            ),
            WidgetInstanceEntity(
                id = "w_notes_1",
                pageIndex = 1,
                widgetType = BuiltInWidgetType.NOTES.id,
                title = "Quick Note",
                spanColumns = 2,
                spanRows = 1,
                orderIndex = 1
            ),
            WidgetInstanceEntity(
                id = "w_calc_2",
                pageIndex = 2,
                widgetType = BuiltInWidgetType.CALCULATOR.id,
                title = "Calculator",
                spanColumns = 2,
                spanRows = 2,
                orderIndex = 0
            )
        )
        widgetDao.upsertAll(defaults)
    }

    suspend fun addBuiltInWidget(type: BuiltInWidgetType, pageIndex: Int) {
        val existing = widgetDao.getAll().filter { it.pageIndex == pageIndex }
        val nextOrder = (existing.maxOfOrNull { it.orderIndex } ?: -1) + 1
        widgetDao.upsert(
            WidgetInstanceEntity(
                id = "w_${type.id}_${UUID.randomUUID().toString().take(6)}",
                pageIndex = pageIndex,
                widgetType = type.id,
                title = type.title,
                spanColumns = type.defaultSize.cols,
                spanRows = type.defaultSize.rows,
                orderIndex = nextOrder
            )
        )
    }

    suspend fun addAppWidget(appWidgetId: Int, label: String, pageIndex: Int) {
        val existing = widgetDao.getAll().filter { it.pageIndex == pageIndex }
        val nextOrder = (existing.maxOfOrNull { it.orderIndex } ?: -1) + 1
        widgetDao.upsert(
            WidgetInstanceEntity(
                id = "appw_${appWidgetId}_${UUID.randomUUID().toString().take(4)}",
                pageIndex = pageIndex,
                widgetType = "android_app_widget",
                title = label,
                spanColumns = 2,
                spanRows = 1,
                orderIndex = nextOrder,
                isAppWidget = true,
                appWidgetId = appWidgetId
            )
        )
    }

    suspend fun removeWidget(id: String) {
        widgetDao.deleteById(id)
    }

    suspend fun resizeWidget(entity: WidgetInstanceEntity, newSize: WidgetSize) {
        widgetDao.upsert(
            entity.copy(
                spanColumns = newSize.cols,
                spanRows = newSize.rows
            )
        )
    }

    suspend fun moveWidget(entity: WidgetInstanceEntity, direction: Int) {
        val pageWidgets = widgetDao.getAll()
            .filter { it.pageIndex == entity.pageIndex }
            .sortedBy { it.orderIndex }
            .toMutableList()
        val idx = pageWidgets.indexOfFirst { it.id == entity.id }
        val target = idx + direction
        if (idx in pageWidgets.indices && target in pageWidgets.indices) {
            val item = pageWidgets.removeAt(idx)
            pageWidgets.add(target, item)
            val reindexed = pageWidgets.mapIndexed { index, w -> w.copy(orderIndex = index) }
            widgetDao.upsertAll(reindexed)
        }
    }

    suspend fun resetToDefaults() {
        widgetDao.clearAll()
        seeded = true
        seedDefaultWidgets()
    }
}
