package com.zdmgold.cleankoach.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zdmgold.cleankoach.core.data.db.entity.CleanupHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanupHistoryDao {

    @Query("SELECT * FROM cleanup_history ORDER BY performedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<CleanupHistoryEntity>>

    @Query("SELECT COALESCE(SUM(freedBytes), 0) FROM cleanup_history")
    fun observeTotalFreed(): Flow<Long>

    @Insert
    suspend fun insert(entity: CleanupHistoryEntity): Long

    @Query("DELETE FROM cleanup_history")
    suspend fun clearAll()
}
