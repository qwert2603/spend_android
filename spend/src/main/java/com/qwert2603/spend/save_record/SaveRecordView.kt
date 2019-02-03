package com.qwert2603.spend.save_record

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.utils.Wrapper
import io.reactivex.Observable

interface SaveRecordView : BaseView<SaveRecordViewState> {
    fun categoryNameChanges(): Observable<String>
    fun kindChanges(): Observable<String>
    fun valueChanges(): Observable<Int>

    fun onDateSelected(): Observable<Wrapper<SDate>>
    fun onTimeSelected(): Observable<Wrapper<STime>>
    fun onCategoryUuidSelected(): Observable<String>
    fun onCategoryUuidAndKindSelected(): Observable<Pair<String, String>>

    fun selectCategoryClicks(): Observable<Any>
    fun selectKindClicks(): Observable<Any>
    fun selectDateClicks(): Observable<Any>
    fun selectTimeClicks(): Observable<Any>

    fun onCategoryInputClicked(): Observable<Any>
    fun onKindInputClicked(): Observable<Any>

    fun onServerCategoryResolved(): Observable<Boolean> // is accept from server
    fun onServerKindResolved(): Observable<Boolean> // is accept from server
    fun onServerDateResolved(): Observable<Boolean> // is accept from server
    fun onServerTimeResolved(): Observable<Boolean> // is accept from server
    fun onServerValueResolved(): Observable<Boolean> // is accept from server

    fun saveClicks(): Observable<Any>
}