package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.entity.toCreatingSpend
import com.qwert2603.spenddemo.model.entity.toSpend
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import io.reactivex.Completable
import io.reactivex.Single

class RemoteItemsDataSourceImpl(
        private val remoteDBFacade: RemoteDBFacade
) : RemoteItemsDataSource<Long, SyncingSpend, RemoteSpend> {

    override fun getAll(lastUpdate: Long): Single<List<RemoteSpend>> = Single.fromCallable {
        remoteDBFacade.getAllSpends(lastUpdate)
    }

    override fun add(t: SyncingSpend): Single<SyncingSpend> {
        val creatingSpend = t.toSpend().toCreatingSpend()//todo: make 1 mapping.
        return Single
                .fromCallable { remoteDBFacade.insertSpend(creatingSpend) }
                .map { creatingSpend.toSpend(it).toSyncingSpend() }
    }

    override fun edit(t: SyncingSpend): Completable = Completable.fromAction {
        remoteDBFacade.updateSpend(t.toSpend())
    }

    override fun remove(id: Long): Completable = Completable.fromAction {
        remoteDBFacade.deleteSpend(id)
    }
}