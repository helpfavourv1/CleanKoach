package com.zdmgold.cleankoach.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zdmgold.cleankoach.core.data.db.entity.DuplicateGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DuplicateGroupDao {

    @Query("SELECT * FROM duplicate_groups ORDER BY totalBytes DESC")
    fun observeAll(): Flow<List<DuplicateGroupEntity>>

    @Query("SELECT * FROM duplicate_groups ORDER BY totalBytes DESC")
    suspend fun getAll(): List<DuplicateGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DuplicateGroupEntity>)

    @Query("DELETE FROM duplicate_groups")
    suspend fun clearAll()
}
