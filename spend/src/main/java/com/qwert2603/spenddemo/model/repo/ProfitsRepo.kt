package com.qwert2603.spenddemo.model.repo

import android.support.annotation.WorkerThread
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

    fun locallyEditedProfits(): SingleLiveEvent<Profit>

    @WorkerThread
    fun getDumpText(): String
}