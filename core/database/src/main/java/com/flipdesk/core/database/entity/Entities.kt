package com.flipdesk.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "widget_instances")
data class WidgetInstanceEntity(
    @PrimaryKey val id: String,
    val pageIndex: Int,
    val widgetType: String,
    val title: String,
    val spanColumns: Int,
    val spanRows: Int,
    val orderIndex: Int,
    val configJson: String = "{}",
    val isAppWidget: Boolean = false,
    val appWidgetId: Int = -1
)

@Serializable
@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val packageName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val folderName: String? = null,
    val sortOrder: Int = 0,
    val lastLaunchedAt: Long = 0L
)

@Serializable
@Entity(tableName = "theme_profiles")
data class ThemeProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val configJson: String,
    val isActive: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueAtMillis: Long,
    val isCompleted: Boolean = false
)
