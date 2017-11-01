package com.qwert2603.spenddemo.base_mvi.load_refresh.list

import com.qwert2603.spenddemo.base_mvi.load_refresh.InitialModelHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRView
import io.reactivex.Observable

interface ListView<in M> : LRView<M> where M : InitialModelHolder<*>, M : ListModelHolder<*> {
    fun loadNextPage(): Observable<Any>
}