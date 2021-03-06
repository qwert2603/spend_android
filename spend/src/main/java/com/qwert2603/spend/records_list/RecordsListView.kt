package com.qwert2603.spend.records_list

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spend.model.entity.Days
import com.qwert2603.spend.model.entity.Minutes
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.utils.Wrapper
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

    fun sortByValueChanges(): Observable<Boolean>
    fun showFiltersChanges(): Observable<Boolean>

    fun longSumPeriodSelected(): Observable<Days>
    fun shortSumPeriodSelected(): Observable<Minutes>

    fun addStubRecordsClicks(): Observable<Any>
    fun clearAllClicks(): Observable<Any>

    fun cancelSelection(): Observable<Any>

    fun deleteSelectedClicks(): Observable<Any>
    fun combineSelectedClicks(): Observable<Any>
    fun changeSelectedClicks(): Observable<Any>

    fun searchQueryChanges(): Observable<String>
    fun selectStartDateClicks(): Observable<Any>
    fun selectEndDateClicks(): Observable<Any>
    fun startDateSelected(): Observable<Wrapper<SDate>>
    fun endDateSelected(): Observable<Wrapper<SDate>>
}