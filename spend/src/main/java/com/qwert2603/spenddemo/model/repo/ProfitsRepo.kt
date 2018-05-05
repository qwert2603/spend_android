package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import io.reactivex.Completable
import io.reactivex.Single

interface ProfitsRepo {
    fun getAllProfits(): Single<List<Profit>>

    /** #@return id of created profit */
    fun addProfit(creatingProfit: CreatingProfit): Single<Long>

    fun removeProfit(profitId: Long): Completable

    fun getDumpText(): Single<String>
}