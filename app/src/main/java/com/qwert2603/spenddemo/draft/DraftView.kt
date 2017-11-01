package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.BaseView
import io.reactivex.Observable
import java.util.*

interface DraftView : BaseView<DraftViewState> {
    fun viewCreated():Observable<Any>
    fun kingChanges(): Observable<String>
    fun valueChanges(): Observable<Int>
    fun dateChanges(): Observable<Date>
    fun saveClicks(): Observable<Any>
}