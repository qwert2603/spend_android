package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.syncprocessor.LocalItemsDataSourceImpl
import com.qwert2603.spenddemo.model.syncprocessor.LoggerImpl
import com.qwert2603.spenddemo.model.syncprocessor.SyncingRecord
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.logger.Logger
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindSyncDataSourcesModule {
    @Binds fun bind0(localItemsDataSourceImpl: LocalItemsDataSourceImpl): LocalItemsDataSource<Long, SyncingRecord>
    @Binds fun bind1(loggerImpl: LoggerImpl): Logger
}