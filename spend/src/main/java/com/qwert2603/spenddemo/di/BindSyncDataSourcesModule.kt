package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.syncprocessor.*
import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.logger.Logger
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface  BindSyncDataSourcesModule {
    @Binds fun bind1(localItemsDataSourceImpl: LocalItemsDataSourceImpl): LocalItemsDataSource<Long, SyncingRecord>
    @Binds fun bind2(localChangesDataSourceImpl: LocalChangesDataSourceImpl): LocalChangesDataSource<Long>
    @Binds fun bind3(lastUpdateRepoImpl: LastUpdateRepoImpl): LastUpdateRepo
    @Binds fun bind4(loggerImpl: LoggerImpl): Logger
}