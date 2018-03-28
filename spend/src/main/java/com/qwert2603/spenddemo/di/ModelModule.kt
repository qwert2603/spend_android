package com.qwert2603.spenddemo.di

import android.arch.persistence.room.Room
import android.content.Context
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.di.qualifiers.RemoteTableName
import com.qwert2603.spenddemo.model.ServerType
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import com.qwert2603.spenddemo.model.syncprocessor.*
import com.qwert2603.spenddemo.utils.sortedByDescending
import com.qwert2603.syncprocessor.SyncProcessor
import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.logger.Logger
import com.qwert2603.syncprocessor.stub.StubLastUpdateRepo
import com.qwert2603.syncprocessor.stub.StubLocalChangesDataSource
import com.qwert2603.syncprocessor.stub.StubRemoteItemsDataSource
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
    @RemoteTableName
    fun remoteTableName(): String = when (BuildConfig.SERVER_TYPE) {
        ServerType.NO_SERVER -> "nth"
        ServerType.SERVER_TEST -> "test_spend"
        ServerType.SERVER_PROD -> "spend"
    }

    @Provides
    @Singleton
    fun remoteItemsDataSource(remoteDBFacade: RemoteDBFacade): RemoteItemsDataSource<Long, SyncingRecord, RemoteRecord> =
            when (BuildConfig.SERVER_TYPE) {
                ServerType.NO_SERVER -> StubRemoteItemsDataSource()
                ServerType.SERVER_TEST -> RemoteItemsDataSourceImpl(remoteDBFacade)
                ServerType.SERVER_PROD -> RemoteItemsDataSourceImpl(remoteDBFacade)
            }

    @Provides
    @Singleton
    fun localChangesDataSource(localDB: LocalDB): LocalChangesDataSource<Long> =
            when (BuildConfig.SERVER_TYPE) {
                ServerType.NO_SERVER -> StubLocalChangesDataSource()
                ServerType.SERVER_TEST -> LocalChangesDataSourceImpl(localDB)
                ServerType.SERVER_PROD -> LocalChangesDataSourceImpl(localDB)
            }

    @Provides
    @Singleton
    fun lastUpdateRepo(appContext: Context): LastUpdateRepo =
            when (BuildConfig.SERVER_TYPE) {
                ServerType.NO_SERVER -> StubLastUpdateRepo()
                ServerType.SERVER_TEST -> LastUpdateRepoImpl(appContext)
                ServerType.SERVER_PROD -> LastUpdateRepoImpl(appContext)
            }

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
            sortFun = { it.sortedByDescending({ it.date }, { it.id }) }
    )
}