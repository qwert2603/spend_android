package com.qwert2603.spenddemo.change_records

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable

interface ChangeRecordsView : BaseView<ChangeRecordsViewState> {
    fun askToSelectDateClicks(): Observable<Any>
    fun askToSelectTimeClicks(): Observable<Any>

    fun changedDateSelected(): Observable<Wrapper<SDate>>
    fun changedTimeSelected(): Observable<Wrapper<Wrapper<STime>>>

    fun changeClicks(): Observable<Any>
}