package com.qwert2603.syncprocessor.stub

import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.RemoteItem
import io.reactivex.Completable
import io.reactivex.Single

class StubRemoteItemsDataSource<I : Any, T : Identifiable<I>, R>
    : RemoteItemsDataSource<I, T, R>  where R : Identifiable<I>, R : RemoteItem {
    override fun getAll(lastUpdate: Long): Single<List<R>> = Single.just(emptyList())
    override fun add(t: T): Single<T> = Single.just(t)
    override fun edit(t: T): Completable = Completable.complete()
    override fun remove(id: I): Completable = Completable.complete()
}