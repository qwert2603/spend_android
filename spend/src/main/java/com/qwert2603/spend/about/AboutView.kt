package com.qwert2603.spend.about

import com.qwert2603.andrlib.base.mvi.BaseView
import io.reactivex.Observable

interface AboutView : BaseView<AboutViewState> {
    fun sendDumpClicks(): Observable<Any>
    fun oldRecordsLockInput(): Observable<Boolean>
}