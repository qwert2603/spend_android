package com.qwert2603.syncprocessor.stub

import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.entity.Identifiable
import io.reactivex.Completable
import io.reactivex.Single

class StubLocalItemsDataSource<I, T : Identifiable<I>> : LocalItemsDataSource<I, T> {
    override fun getAll(): Single<List<T>> = Single.just(emptyList())
    override fun changeId(oldId: I, newItem: T): Completable = Completable.complete()
    override fun save(t: T): Completable = Completable.complete()
    override fun remove(id: I): Completable = Completable.complete()
}