package com.zdmgold.cleankoach.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_hashes",
    indices = [Index(value = ["sha256"]), Index(value = ["dhash"])]
)
data class MediaHashEntity(
    @PrimaryKey val mediaId: Long,
    val sha256: String,
    val dhash: Long,
    val sharpness: Float,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String,
    val dateAdded: Long,
    val computedAt: Long
)
