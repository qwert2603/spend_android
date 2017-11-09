package com.qwert2603.syncprocessor.datasource

import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.RemoteItem
import io.reactivex.Completable
import io.reactivex.Single

interface RemoteItemsDataSource<I : Any, T : Identifiable<I>, R> where R : Identifiable<I>, R : RemoteItem {
    fun getAll(lastUpdate: Long): Single<List<R>>
    fun add(t: T): Single<T>
    fun edit(t: T): Completable
    fun remove(id: I): Completable
}