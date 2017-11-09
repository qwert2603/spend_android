package com.qwert2603.syncprocessor.datasource

import com.qwert2603.syncprocessor.entity.Identifiable
import io.reactivex.Completable
import io.reactivex.Single

interface LocalItemsDataSource<I : Any, T : Identifiable<I>> {
    fun getAll(): Single<List<T>>
    fun changeId(oldId: I, newItem: T): Completable
    fun save(t: T): Completable
    fun remove(id: I): Completable
}