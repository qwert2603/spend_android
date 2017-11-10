package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.entity.toCreatingRecord
import com.qwert2603.spenddemo.model.entity.toRecord
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import io.reactivex.Completable
import io.reactivex.Single

class RemoteItemsDataSourceImpl(
        private val remoteDBFacade: RemoteDBFacade
) : RemoteItemsDataSource<Long, SyncingRecord, RemoteRecord> {

    override fun getAll(lastUpdate: Long): Single<List<RemoteRecord>> = Single.fromCallable {
        remoteDBFacade.getAllRecords(lastUpdate)
    }

    override fun add(t: SyncingRecord): Single<SyncingRecord> {
        val creatingRecord = t.toRecord().toCreatingRecord()//todo: make 1 mapping.
        return Single
                .fromCallable { remoteDBFacade.insertRecord(creatingRecord) }
                .map { creatingRecord.toRecord(it).toSyncingRecord() }
    }

    override fun edit(t: SyncingRecord): Completable = Completable.fromAction {
        remoteDBFacade.updateRecord(t.toRecord())
    }

    override fun remove(id: Long): Completable = Completable.fromAction {
        remoteDBFacade.deleteRecord(id)
    }
}