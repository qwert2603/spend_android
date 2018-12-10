package com.qwert2603.spenddemo.about

import com.qwert2603.andrlib.base.mvi.BaseView
import io.reactivex.Observable

interface AboutView : BaseView<AboutViewState> {
    fun sendDumpClicks(): Observable<Any>
}