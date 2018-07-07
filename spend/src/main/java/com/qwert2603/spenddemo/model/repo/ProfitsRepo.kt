package com.qwert2603.spenddemo.model.repo

import android.arch.lifecycle.LiveData
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.utils.SingleLiveEvent

interface ProfitsRepo {
    fun addProfit(creatingProfit: CreatingProfit)

    fun addProfits(profits: List<CreatingProfit>)

    fun editProfit(profit: Profit)

    fun removeProfit(profitId: Long)

    fun removeAllProfits()

    fun locallyCreatedProfits(): SingleLiveEvent<Profit>

    fun syncingProfitIds(): LiveData<Set<Long>>

    suspend fun getDumpText(): String

    fun getSumLastDays(days: Int): LiveData<Long>

    fun getSumLastMinutes(minutes: Int): LiveData<Long>
}