package com.qwert2603.syncprocessor

import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemEvent
import com.qwert2603.syncprocessor.entity.ItemsState
import io.reactivex.Observable

interface ISyncProcessor<I : Any, T : Identifiable<I>> {
    fun itemsState(): Observable<ItemsState<I, T>>
    fun itemEvents(): Observable<Pair<T, ItemEvent>>

    fun addItem(item: T)
    fun editItem(item: T)
    fun removeItem(itemId: I)
}