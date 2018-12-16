package com.qwert2603.spenddemo.sums

import com.qwert2603.andrlib.base.mvi.BaseView
import io.reactivex.Observable

interface SumsView : BaseView<SumsViewState> {
    fun showDaySums(): Observable<Boolean>
    fun showMonthSums(): Observable<Boolean>
    fun showYearSums(): Observable<Boolean>
    fun showBalances(): Observable<Boolean>

    fun clearAllClicks(): Observable<Any>
}