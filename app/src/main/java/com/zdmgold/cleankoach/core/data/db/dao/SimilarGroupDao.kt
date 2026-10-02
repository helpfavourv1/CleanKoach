package com.zdmgold.cleankoach.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zdmgold.cleankoach.core.data.db.entity.SimilarGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimilarGroupDao {

    @Query("SELECT * FROM similar_groups ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SimilarGroupEntity>>

    @Query("SELECT * FROM similar_groups ORDER BY createdAt DESC")
    suspend fun getAll(): List<SimilarGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SimilarGroupEntity>)

    @Query("DELETE FROM similar_groups")
    suspend fun clearAll()
}
