package com.zdmgold.cleankoach.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "duplicate_groups",
    indices = [Index(value = ["sha256"])]
)
data class DuplicateGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sha256: String,
    val mediaIds: String,
    val totalBytes: Long,
    val createdAt: Long
)
