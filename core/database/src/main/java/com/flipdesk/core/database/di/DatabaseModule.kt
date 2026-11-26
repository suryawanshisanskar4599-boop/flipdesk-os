package com.flipdesk.core.database

import android.content.Context
import androidx.room.Room
import com.flipdesk.core.database.dao.AppPreferenceDao
import com.flipdesk.core.database.dao.QuickNoteDao
import com.flipdesk.core.database.dao.ReminderDao
import com.flipdesk.core.database.dao.ThemeProfileDao
import com.flipdesk.core.database.dao.WidgetInstanceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlipDeskDatabase =
        Room.databaseBuilder(
            context,
            FlipDeskDatabase::class.java,
            "flipdesk_os.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideWidgetInstanceDao(db: FlipDeskDatabase): WidgetInstanceDao = db.widgetInstanceDao()

    @Provides
    fun provideAppPreferenceDao(db: FlipDeskDatabase): AppPreferenceDao = db.appPreferenceDao()

    @Provides
    fun provideThemeProfileDao(db: FlipDeskDatabase): ThemeProfileDao = db.themeProfileDao()

    @Provides
    fun provideQuickNoteDao(db: FlipDeskDatabase): QuickNoteDao = db.quickNoteDao()

    @Provides
    fun provideReminderDao(db: FlipDeskDatabase): ReminderDao = db.reminderDao()
}
