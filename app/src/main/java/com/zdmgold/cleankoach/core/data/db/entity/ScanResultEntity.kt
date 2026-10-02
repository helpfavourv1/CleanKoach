package com.zdmgold.cleankoach.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey val scanType: String,
    val lastScannedAt: Long,
    val itemCount: Int,
    val totalBytes: Long
)
