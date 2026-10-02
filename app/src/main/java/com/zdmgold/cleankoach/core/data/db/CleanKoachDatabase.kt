package com.zdmgold.cleankoach.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zdmgold.cleankoach.core.data.db.dao.CleanupHistoryDao
import com.zdmgold.cleankoach.core.data.db.dao.DuplicateGroupDao
import com.zdmgold.cleankoach.core.data.db.dao.MediaHashDao
import com.zdmgold.cleankoach.core.data.db.dao.ScanResultDao
import com.zdmgold.cleankoach.core.data.db.dao.SimilarGroupDao
import com.zdmgold.cleankoach.core.data.db.entity.CleanupHistoryEntity
import com.zdmgold.cleankoach.core.data.db.entity.DuplicateGroupEntity
import com.zdmgold.cleankoach.core.data.db.entity.MediaHashEntity
import com.zdmgold.cleankoach.core.data.db.entity.ScanResultEntity
import com.zdmgold.cleankoach.core.data.db.entity.SimilarGroupEntity

@Database(
    entities = [
        ScanResultEntity::class,
        MediaHashEntity::class,
        DuplicateGroupEntity::class,
        SimilarGroupEntity::class,
        CleanupHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CleanKoachDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun mediaHashDao(): MediaHashDao
    abstract fun duplicateGroupDao(): DuplicateGroupDao
    abstract fun similarGroupDao(): SimilarGroupDao
    abstract fun cleanupHistoryDao(): CleanupHistoryDao
}
