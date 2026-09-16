package com.jpleon.pushtomail.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    @Query("SELECT * FROM triggers ORDER BY id")
    fun observeAll(): Flow<List<TriggerEntity>>

    @Query("SELECT * FROM triggers WHERE enabled = 1")
    suspend fun getEnabled(): List<TriggerEntity>

    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getById(id: Long): TriggerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(trigger: TriggerEntity): Long

    @Delete
    suspend fun delete(trigger: TriggerEntity)

    @Query("UPDATE triggers SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
