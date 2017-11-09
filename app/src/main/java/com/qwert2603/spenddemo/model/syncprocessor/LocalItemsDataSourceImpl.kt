package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toRecord
import com.qwert2603.spenddemo.model.local_db.tables.toRecordTable
import com.qwert2603.spenddemo.utils.mapList
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalItemsDataSourceImpl @Inject constructor(
        private val localDB: LocalDB
) : LocalItemsDataSource<Long, SyncingRecord> {
    override fun getAll(): Single<List<SyncingRecord>> = localDB.recordsDao()
            .getAllRecords()
            .mapList { it.toRecord().toSyncingRecord() }

    override fun changeId(oldId: Long, newItem: SyncingRecord): Completable = Completable.fromAction {
        localDB.recordsDao().updateRecordId(oldId, newItem.toRecord().toRecordTable())
    }

    override fun save(t: SyncingRecord): Completable = Completable.fromAction {
        localDB.recordsDao().editRecord(t.toRecord().toRecordTable())
    }

    override fun remove(id: Long): Completable = Completable.fromAction {
        localDB.recordsDao().removeRecord(id)
    }
}