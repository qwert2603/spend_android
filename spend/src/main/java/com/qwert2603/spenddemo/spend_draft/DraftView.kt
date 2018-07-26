package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import java.util.*

interface DraftView : BaseView<DraftViewState> {
    fun viewCreated(): Observable<Any>

    fun kingChanges(): Observable<String>
    fun valueChanges(): Observable<Int>

    fun saveClicks(): Observable<Any>

    fun selectDateClicks(): Observable<Any>
    fun selectTimeClicks(): Observable<Any>
    fun selectKindClicks(): Observable<Any>

    fun onDateSelected(): Observable<Wrapper<Date>>
    fun onTimeSelected(): Observable<Wrapper<Date>>
    fun onKindSelected(): Observable<String>

    fun onKindInputClicked(): Observable<Any>

    fun onKindSuggestionSelected(): Observable<String>
}