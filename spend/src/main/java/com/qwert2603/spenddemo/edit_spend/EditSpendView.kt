package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.BaseView
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import java.util.*

interface EditSpendView : BaseView<EditSpendViewState> {
    fun viewCreated(): Observable<Any>

    fun kindChanges(): Observable<String>
    fun valueChanges(): Observable<Int>

    fun onDateSelected(): Observable<Date>
    fun onTimeSelected(): Observable<Wrapper<Date>>
    fun onKindSelected(): Observable<String>

    fun selectKindClicks(): Observable<Any>
    fun selectDateClicks(): Observable<Any>
    fun selectTimeClicks(): Observable<Any>

    fun onServerKindResolved(): Observable<Boolean> // is accept from server
    fun onServerDateResolved(): Observable<Boolean> // is accept from server
    fun onServerTimeResolved(): Observable<Boolean> // is accept from server
    fun onServerValueResolved(): Observable<Boolean> // is accept from server

    fun saveClicks(): Observable<Any>
}