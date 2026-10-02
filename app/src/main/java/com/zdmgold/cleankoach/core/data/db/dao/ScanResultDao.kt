package com.zdmgold.cleankoach.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zdmgold.cleankoach.core.data.db.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Query("SELECT * FROM scan_results WHERE scanType = :scanType LIMIT 1")
    suspend fun get(scanType: String): ScanResultEntity?

    @Query("SELECT * FROM scan_results")
    fun observeAll(): Flow<List<ScanResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScanResultEntity)

    @Query("DELETE FROM scan_results WHERE scanType = :scanType")
    suspend fun clear(scanType: String)

    @Query("DELETE FROM scan_results")
    suspend fun clearAll()
}
