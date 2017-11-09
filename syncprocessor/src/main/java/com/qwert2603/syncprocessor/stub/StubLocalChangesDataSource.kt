package com.qwert2603.syncprocessor.stub

import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.entity.Change
import io.reactivex.Completable
import io.reactivex.Single

class StubLocalChangesDataSource<I : Any> : LocalChangesDataSource<I> {
    override fun getAll(): Single<List<Change<I>>> = Single.just(emptyList())
    override fun save(change: Change<I>): Completable = Completable.complete()
    override fun remove(itemId: I): Completable = Completable.complete()
}