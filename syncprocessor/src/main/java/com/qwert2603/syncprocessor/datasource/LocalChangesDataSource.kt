package com.qwert2603.syncprocessor.datasource

import com.qwert2603.syncprocessor.entity.Change
import io.reactivex.Completable
import io.reactivex.Single

interface LocalChangesDataSource<I : Any> {
    fun getAll(): Single<List<Change<I>>>
    fun save(change: Change<I>): Completable
    fun remove(itemId: I): Completable
}