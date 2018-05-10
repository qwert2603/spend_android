package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.toProfit
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toProfit
import com.qwert2603.spenddemo.model.local_db.tables.toProfitTable
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.PrefsCounter
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider,
        appContext: Context
) : ProfitsRepo {

    private val localIdCounter = PrefsCounter(
            prefs = appContext.getSharedPreferences("profits.prefs", Context.MODE_PRIVATE),
            key = "last_profit_local_id"
    )

    override fun getAllProfits(): Single<List<Profit>> = localDB.profitsDao()
            .getAllProfits()
            .mapList { it.toProfit() }
            .subscribeOn(modelSchedulersProvider.io)

    override fun addProfit(creatingProfit: CreatingProfit): Single<Long> = Single
            .fromCallable {
                val localId = localIdCounter.getNext()
                val profit = creatingProfit.toProfit(localId).toProfitTable()
                localDB.profitsDao().addProfit(profit)
                return@fromCallable localId
            }
            .subscribeOn(modelSchedulersProvider.io)

    override fun editProfit(profit: Profit): Completable = Completable
            .fromAction { localDB.profitsDao().editProfit(profit.toProfitTable()) }
            .subscribeOn(modelSchedulersProvider.io)

    override fun removeProfit(profitId: Long): Completable = Completable
            .fromAction { localDB.profitsDao().removeProfit(profitId) }
            .subscribeOn(modelSchedulersProvider.io)

    override fun removeAllProfits(): Completable = Completable
            .fromAction { localDB.profitsDao().removeAllProfits() }
            .subscribeOn(modelSchedulersProvider.io)

    override fun getDumpText(): Single<String> = getAllProfits()
            .map {
                if (it.isEmpty()) return@map "nth"
                it
                        .reversed()
                        .map {
                            listOf(
                                    it.kind,
                                    Const.DATE_FORMAT.format(it.date),
                                    it.value.toString()
                            ).reduce { s1, s2 -> "$s1,$s2" }
                        }
                        .reduce { s1, s2 -> "$s1\n$s2" }
            }
}