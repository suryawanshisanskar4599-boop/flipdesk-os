package com.flipdesk.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flipdesk.core.database.entity.AppPreferenceEntity
import com.flipdesk.core.database.entity.QuickNoteEntity
import com.flipdesk.core.database.entity.ReminderEntity
import com.flipdesk.core.database.entity.ThemeProfileEntity
import com.flipdesk.core.database.entity.WidgetInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetInstanceDao {
    @Query("SELECT * FROM widget_instances ORDER BY pageIndex ASC, orderIndex ASC")
    fun observeAll(): Flow<List<WidgetInstanceEntity>>

    @Query("SELECT * FROM widget_instances ORDER BY pageIndex ASC, orderIndex ASC")
    suspend fun getAll(): List<WidgetInstanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WidgetInstanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<WidgetInstanceEntity>)

    @Query("DELETE FROM widget_instances WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM widget_instances")
    suspend fun clearAll()
}

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences")
    fun observeAll(): Flow<List<AppPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppPreferenceEntity)

    @Query("UPDATE app_preferences SET lastLaunchedAt = :timestamp WHERE packageName = :packageName")
    suspend fun updateLastLaunched(packageName: String, timestamp: Long)
}

@Dao
interface ThemeProfileDao {
    @Query("SELECT * FROM theme_profiles ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ThemeProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ThemeProfileEntity)

    @Query("DELETE FROM theme_profiles WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<QuickNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuickNoteEntity)

    @Query("DELETE FROM quick_notes WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueAtMillis ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)
}
