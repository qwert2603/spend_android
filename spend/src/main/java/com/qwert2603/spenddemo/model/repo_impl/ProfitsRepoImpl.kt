package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.support.annotation.WorkerThread
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.toProfit
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toProfitTable
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.PrefsCounter
import com.qwert2603.spenddemo.utils.SingleLiveEvent
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val dbExecutor: Executor,
        appContext: Context
) : ProfitsRepo {

    private val localIdCounter = PrefsCounter(
            prefs = appContext.getSharedPreferences("profits.prefs", Context.MODE_PRIVATE),
            key = "last_profit_local_id",
            defaultValue = 1_000_000
    )

    private val locallyCreatedProfits = SingleLiveEvent<Profit>()

    override fun addProfits(profits: List<CreatingProfit>) {
        dbExecutor.execute {
            localDB.profitsDao().addProfits(profits.map {
                it.toProfit(localIdCounter.getNext()).toProfitTable()
            })
        }
    }

    override fun addProfit(creatingProfit: CreatingProfit) {
        dbExecutor.execute {
            val localId = localIdCounter.getNext()
            val profit = creatingProfit.toProfit(localId)
            locallyCreatedProfits.postValue(profit)
            localDB.profitsDao().addProfit(profit.toProfitTable())
        }
    }

    override fun editProfit(profit: Profit) {
        dbExecutor.execute { localDB.profitsDao().editProfit(profit.toProfitTable()) }
    }

    override fun removeProfit(profitId: Long) {
        dbExecutor.execute { localDB.profitsDao().deleteProfit(profitId) }
    }

    override fun removeAllProfits() {
        dbExecutor.execute { localDB.profitsDao().deleteAllProfits() }
    }

    override fun locallyCreatedProfits(): SingleLiveEvent<Profit> = locallyCreatedProfits

    @WorkerThread
    override fun getDumpText(): String = localDB.profitsDao()
            .getAllProfitsList()
            .let {
                if (it.isEmpty()) return@let "nth"
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