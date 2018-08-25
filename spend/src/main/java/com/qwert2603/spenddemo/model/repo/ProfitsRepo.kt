package com.qwert2603.spenddemo.model.repo

import android.arch.lifecycle.LiveData
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.FullSyncStatus
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.utils.SingleLiveEvent
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable

interface ProfitsRepo {
    fun addProfit(creatingProfit: CreatingProfit)

    fun addProfits(profits: List<CreatingProfit>)

    fun editProfit(profit: Profit)

    fun removeProfit(profitId: Long)

    fun removeAllProfits()

    fun getProfit(id: Long): Observable<Wrapper<Profit>>

    fun locallyCreatedProfits(): SingleLiveEvent<Profit>

    fun syncingProfitIds(): LiveData<Set<Long>>

    fun syncStatus(): LiveData<FullSyncStatus>

    suspend fun getDumpText(): String

    fun getSumLastDays(days: Int): LiveData<Long>

    fun getSumLastMinutes(minutes: Int): LiveData<Long>

    fun getChangesCount(): LiveData<Int>
}