package com.zdmgold.cleankoach.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "similar_groups")
data class SimilarGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaIds: String,
    val bestMediaId: Long,
    val createdAt: Long
)
