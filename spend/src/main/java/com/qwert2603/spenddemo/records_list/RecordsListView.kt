package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.Days
import com.qwert2603.spenddemo.model.entity.Minutes
import com.qwert2603.spenddemo.model.entity.Record
import io.reactivex.Observable

interface RecordsListView : BaseView<RecordsListViewState> {
    fun recordClicks(): Observable<Record>
    fun recordLongClicks(): Observable<Record>

    fun editRecordClicks(): Observable<String>
    fun deleteRecordClicks(): Observable<String>

    fun createProfitClicks(): Observable<Any>

    fun chooseLongSumPeriodClicks(): Observable<Any>
    fun chooseShortSumPeriodClicks(): Observable<Any>

    fun showSpendsChanges(): Observable<Boolean>
    fun showProfitsChanges(): Observable<Boolean>
    fun showSumsChanges(): Observable<Boolean>

    fun showChangeKindsChanges(): Observable<Boolean>
    fun showTimesChanges(): Observable<Boolean>

    fun sortByValueChanges(): Observable<Boolean>

    fun longSumPeriodSelected(): Observable<Days>
    fun shortSumPeriodSelected(): Observable<Minutes>

    fun addStubRecordsClicks(): Observable<Any>
    fun clearAllClicks(): Observable<Any>

    fun cancelSelection(): Observable<Any>

    fun deleteSelectedClicks(): Observable<Any>
    fun combineSelectedClicks(): Observable<Any>
    fun changeSelectedClicks(): Observable<Any>
}