package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.Record
import io.reactivex.Observable

interface RecordsListView : BaseView<RecordsListViewState> {
    fun recordClicks(): Observable<Record>
    fun recordLongClicks(): Observable<Record>

    fun createProfitClicks(): Observable<Any>

    fun chooseLongSumPeriodClicks(): Observable<Any>
    fun chooseShortSumPeriodClicks(): Observable<Any>

    fun showSpendsChanges(): Observable<Boolean>
    fun showProfitsChanges(): Observable<Boolean>
    fun showSumsChanges(): Observable<Boolean>

    fun showChangeKindsChanges(): Observable<Boolean>
    fun showTimesChanges(): Observable<Boolean>

    fun longSumPeriodDaysSelected(): Observable<Int>
    fun shortSumPeriodMinutesSelected(): Observable<Int>

    fun makeDumpClicks(): Observable<Any>
    fun addStubRecordsClicks(): Observable<Any>
    fun clearAllClicks(): Observable<Any>
}