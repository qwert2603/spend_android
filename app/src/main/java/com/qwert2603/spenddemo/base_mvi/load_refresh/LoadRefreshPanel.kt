package com.qwert2603.spenddemo.base_mvi.load_refresh

import io.reactivex.Observable

interface LoadRefreshPanel {
    fun retryClicks(): Observable<Any>
    fun refreshes(): Observable<Any>

    fun render(vs: LRViewState<*>)
}