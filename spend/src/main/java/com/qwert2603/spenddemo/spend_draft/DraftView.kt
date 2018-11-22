package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable

interface DraftView : BaseView<DraftViewState> {
    fun viewCreated(): Observable<Any>

    fun kingChanges(): Observable<String>
    fun valueChanges(): Observable<Int>

    fun saveClicks(): Observable<Any>

    fun selectDateClicks(): Observable<Any>
    fun selectTimeClicks(): Observable<Any>
    fun selectKindClicks(): Observable<Any>

    fun onDateSelected(): Observable<Wrapper<SDate>>
    fun onTimeSelected(): Observable<Wrapper<STime>>
    fun onKindSelected(): Observable<String>

    fun onKindInputClicked(): Observable<Any>

    fun onKindSuggestionSelected(): Observable<String>
}