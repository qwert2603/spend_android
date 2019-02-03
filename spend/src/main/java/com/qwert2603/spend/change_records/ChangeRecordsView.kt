package com.qwert2603.spend.change_records

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.utils.Wrapper
import io.reactivex.Observable

interface ChangeRecordsView : BaseView<ChangeRecordsViewState> {
    fun askToSelectDateClicks(): Observable<Any>
    fun askToSelectTimeClicks(): Observable<Any>

    fun changedDateSelected(): Observable<Wrapper<SDate>>
    fun changedTimeSelected(): Observable<Wrapper<Wrapper<STime>>>

    fun changeClicks(): Observable<Any>
}