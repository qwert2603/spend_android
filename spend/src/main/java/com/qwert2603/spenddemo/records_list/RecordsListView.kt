package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.BaseView
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import io.reactivex.Observable

interface RecordsListView : BaseView<RecordsListViewState> {
    fun editRecordClicks(): Observable<RecordUI>
    fun deleteRecordClicks(): Observable<RecordUI>

    fun showChangesClicks(): Observable<Any>

    fun deleteRecordConfirmed(): Observable<Long>
    fun editRecordConfirmed(): Observable<Record>

    fun sendRecordsClicks(): Observable<Any>
    fun showAboutClicks(): Observable<Any>
}