package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import io.reactivex.Observable

interface RecordsListView : BaseView<RecordsListViewState> {
    fun viewCreated(): Observable<Any>

    fun editRecordClicks(): Observable<RecordUI>
    fun deleteRecordClicks(): Observable<RecordUI>

    fun showChangesClicks(): Observable<Any>

    fun deleteRecordConfirmed(): Observable<Long>
    fun editRecordConfirmed(): Observable<Record>

    fun sendRecordsClicks(): Observable<Any>
    fun showAboutClicks(): Observable<Any>

    fun showIdsChanges(): Observable<Boolean>
    fun showChangeKindsChanges(): Observable<Boolean>
    fun showDateSumsChanges(): Observable<Boolean>
    fun showSpendsChanges(): Observable<Boolean>
    fun showProfitsChanges(): Observable<Boolean>

    fun addProfitClicks(): Observable<Any>
    fun deleteProfitClicks(): Observable<ProfitUI>

    fun addProfitConfirmed(): Observable<CreatingProfit>
    fun deleteProfitConfirmed(): Observable<Long>
}