package com.zdmgold.cleankoach.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleanup_history")
data class CleanupHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val performedAt: Long,
    val itemCount: Int,
    val freedBytes: Long
)
