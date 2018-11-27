package com.qwert2603.spenddemo.save_record

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable

interface SaveRecordView : BaseView<SaveRecordViewState> {
    fun kindChanges(): Observable<String>
    fun valueChanges(): Observable<Int>

    fun onDateSelected(): Observable<Wrapper<SDate>>
    fun onTimeSelected(): Observable<Wrapper<STime>>
    fun onKindSelected(): Observable<String>

    fun selectKindClicks(): Observable<Any>
    fun selectDateClicks(): Observable<Any>
    fun selectTimeClicks(): Observable<Any>

    fun onKindInputClicked(): Observable<Any>
    fun onKindSuggestionSelected(): Observable<String>

    fun onServerKindResolved(): Observable<Boolean> // is accept from server
    fun onServerDateResolved(): Observable<Boolean> // is accept from server
    fun onServerTimeResolved(): Observable<Boolean> // is accept from server
    fun onServerValueResolved(): Observable<Boolean> // is accept from server

    fun saveClicks(): Observable<Any>
}