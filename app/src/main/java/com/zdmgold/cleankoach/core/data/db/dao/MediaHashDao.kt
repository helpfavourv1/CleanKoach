package com.zdmgold.cleankoach.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zdmgold.cleankoach.core.data.db.entity.MediaHashEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaHashDao {

    @Query("SELECT * FROM media_hashes WHERE mediaId = :mediaId LIMIT 1")
    suspend fun get(mediaId: Long): MediaHashEntity?

    @Query("SELECT * FROM media_hashes")
    suspend fun getAll(): List<MediaHashEntity>

    @Query("SELECT * FROM media_hashes WHERE sha256 = :sha256")
    suspend fun getBySha256(sha256: String): List<MediaHashEntity>

    @Query("SELECT * FROM media_hashes ORDER BY computedAt DESC")
    fun observeAll(): Flow<List<MediaHashEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<MediaHashEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaHashEntity)

    @Query("DELETE FROM media_hashes")
    suspend fun clearAll()

    @Query("DELETE FROM media_hashes WHERE mediaId NOT IN (:currentIds)")
    suspend fun pruneMissing(currentIds: List<Long>)
}
