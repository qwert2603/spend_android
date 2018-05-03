package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.toProfit
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toProfit
import com.qwert2603.spenddemo.model.local_db.tables.toProfitTable
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : ProfitsRepo {

    override fun getAllProfits(): Single<List<Profit>> = localDB.profitsDao()
            .getAllProfits()
            .mapList { it.toProfit() }
            .subscribeOn(modelSchedulersProvider.io)

    override fun addProfit(creatingProfit: CreatingProfit): Completable = Completable
            .fromAction {
                val localId = Random().nextInt(1_000_000).toLong() // todo
                val profit = creatingProfit.toProfit(localId).toProfitTable()
                localDB.profitsDao().addProfit(profit)
            }
            .subscribeOn(modelSchedulersProvider.io)

    override fun removeProfit(profitId: Long): Completable = Completable
            .fromAction { localDB.profitsDao().removeProfit(profitId) }
            .subscribeOn(modelSchedulersProvider.io)
}