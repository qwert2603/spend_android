package com.qwert2603.spenddemo.di

import android.arch.persistence.room.Room
import android.content.Context
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import com.qwert2603.spenddemo.model.syncprocessor.RemoteRecord
import com.qwert2603.spenddemo.model.syncprocessor.SyncingRecord
import com.qwert2603.spenddemo.model.syncprocessor.toSyncingRecord
import com.qwert2603.syncprocessor.SyncProcessor
import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.logger.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ModelModule {
    @Provides
    @Singleton
    fun localDB(appContext: Context) = Room
            .databaseBuilder(appContext, LocalDB::class.java, "local_db")
            .build()

    @Provides
    @Singleton
    fun remoteDB(): RemoteDB = /*RemoteDBStub()*/ RemoteDBImpl(
            "jdbc:postgresql://192.168.1.26:5432/spend",
            "postgres",
            "1234"
    )

    @Provides
//    @RemoteTableName todo
    fun remoteTableName(): String = BuildConfig.REMOTE_TABLE_NAME

    @Provides
    @Singleton
    fun syncProcessor(
            remoteItemsDataSource: RemoteItemsDataSource<Long, SyncingRecord, RemoteRecord>,
            localItemsDataSource: LocalItemsDataSource<Long, SyncingRecord>,
            localChangesDataSource: LocalChangesDataSource<Long>,
            logger: Logger,
            lastUpdateRepo: LastUpdateRepo
    ): SyncProcessor<Long, SyncingRecord, RemoteRecord> = SyncProcessor(
            remoteItemsDataSource = remoteItemsDataSource,
            localItemsDataSource = localItemsDataSource,
            localChangesDataSource = localChangesDataSource,
            lastUpdateRepo = lastUpdateRepo,
            logger = logger,
            r2t = RemoteRecord::toSyncingRecord,
            sortFun = { it.sortedByDescending { it.date } }//todo: sort by id when day is same.
    )
}