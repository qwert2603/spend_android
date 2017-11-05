package com.qwert2603.syncprocessor.stub

import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.entity.Identifiable
import io.reactivex.Completable
import io.reactivex.Single

class StubRemoteItemsDataSource<I, T : Identifiable<I>> : RemoteItemsDataSource<I, T> {
    override fun getAll(): Single<List<T>> = Single.just(emptyList())
    override fun add(t: T): Single<T> = Single.just(t)
    override fun edit(t: T): Completable = Completable.complete()
    override fun remove(id: I): Completable = Completable.complete()
}