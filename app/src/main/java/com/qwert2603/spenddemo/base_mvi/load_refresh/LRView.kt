package com.qwert2603.spenddemo.base_mvi.load_refresh

import com.qwert2603.spenddemo.base_mvi.BaseView
import io.reactivex.Observable

interface LRView<in M : InitialModelHolder<*>> : BaseView<LRViewState<M>> {
    fun load(): Observable<Any>
    fun retry(): Observable<Any>
    fun refresh(): Observable<Any>
}