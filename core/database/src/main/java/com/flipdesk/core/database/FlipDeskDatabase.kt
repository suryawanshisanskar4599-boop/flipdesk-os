package com.flipdesk.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flipdesk.core.database.dao.AppPreferenceDao
import com.flipdesk.core.database.dao.QuickNoteDao
import com.flipdesk.core.database.dao.ReminderDao
import com.flipdesk.core.database.dao.ThemeProfileDao
import com.flipdesk.core.database.dao.WidgetInstanceDao
import com.flipdesk.core.database.entity.AppPreferenceEntity
import com.flipdesk.core.database.entity.QuickNoteEntity
import com.flipdesk.core.database.entity.ReminderEntity
import com.flipdesk.core.database.entity.ThemeProfileEntity
import com.flipdesk.core.database.entity.WidgetInstanceEntity

@Database(
    entities = [
        WidgetInstanceEntity::class,
        AppPreferenceEntity::class,
        ThemeProfileEntity::class,
        QuickNoteEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlipDeskDatabase : RoomDatabase() {
    abstract fun widgetInstanceDao(): WidgetInstanceDao
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun themeProfileDao(): ThemeProfileDao
    abstract fun quickNoteDao(): QuickNoteDao
    abstract fun reminderDao(): ReminderDao
}
