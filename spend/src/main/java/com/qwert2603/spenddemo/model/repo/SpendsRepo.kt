package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.entity.SpendsState
import io.reactivex.Observable
import io.reactivex.Single

interface SpendsRepo {
    fun addSpend(creatingSpend: CreatingSpend)
    fun editSpend(spend: Spend)
    fun removeSpend(spendId: Long)
    fun removeAllSpends()

    fun spendsState(): Observable<SpendsState>
    fun spendCreatedEvents(): Observable<Spend>

    fun getDumpText(): Single<String>
}

