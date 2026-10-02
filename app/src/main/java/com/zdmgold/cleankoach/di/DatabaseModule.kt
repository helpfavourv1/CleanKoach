package com.zdmgold.cleankoach.di

import android.content.Context
import androidx.room.Room
import com.zdmgold.cleankoach.core.data.db.CleanKoachDatabase
import com.zdmgold.cleankoach.core.data.db.dao.CleanupHistoryDao
import com.zdmgold.cleankoach.core.data.db.dao.DuplicateGroupDao
import com.zdmgold.cleankoach.core.data.db.dao.MediaHashDao
import com.zdmgold.cleankoach.core.data.db.dao.ScanResultDao
import com.zdmgold.cleankoach.core.data.db.dao.SimilarGroupDao
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.prefs.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CleanKoachDatabase =
        Room.databaseBuilder(context, CleanKoachDatabase::class.java, "cleankoach.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideScanResultDao(db: CleanKoachDatabase): ScanResultDao = db.scanResultDao()

    @Provides
    fun provideMediaHashDao(db: CleanKoachDatabase): MediaHashDao = db.mediaHashDao()

    @Provides
    fun provideDuplicateGroupDao(db: CleanKoachDatabase): DuplicateGroupDao = db.duplicateGroupDao()

    @Provides
    fun provideSimilarGroupDao(db: CleanKoachDatabase): SimilarGroupDao = db.similarGroupDao()

    @Provides
    fun provideCleanupHistoryDao(db: CleanKoachDatabase): CleanupHistoryDao = db.cleanupHistoryDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideConsentDataStore(@ApplicationContext context: Context): ConsentDataStore =
        ConsentDataStore(context)
}
