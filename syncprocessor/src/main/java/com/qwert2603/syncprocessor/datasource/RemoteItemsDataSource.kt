package com.qwert2603.syncprocessor.datasource

import com.qwert2603.syncprocessor.entity.Identifiable
import io.reactivex.Completable
import io.reactivex.Single

interface RemoteItemsDataSource<I, T : Identifiable<I>> {
    fun getAll(): Single<List<T>>
    fun add(t: T): Single<T>
    fun edit(t: T): Completable
    fun remove(id: I): Completable
}