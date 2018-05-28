package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.WorkerThread
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.entity.toSpend
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.toSpendTable
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.PrefsCounter
import com.qwert2603.spenddemo.utils.combineLatest
import com.qwert2603.spenddemo.utils.map
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val dbExecutor: Executor,
        appContext: Context
) : SpendsRepo {

    private val localIdCounter = PrefsCounter(
            prefs = appContext.getSharedPreferences("spends.prefs", Context.MODE_PRIVATE),
            key = "last_spend_local_id",
            defaultValue = 1_000_000
    )

    private val locallyCreatedSpends = MutableLiveData<Spend>()

    override fun addSpend(creatingSpend: CreatingSpend) {
        dbExecutor.execute {
            val localId = localIdCounter.getNext()
            val localSpend = creatingSpend.toSpend(localId)
            locallyCreatedSpends.postValue(localSpend)
            localDB.spendsDao().addSpend(localSpend.toSpendTable())
        }
    }

    override fun addSpends(spends: List<CreatingSpend>) {
        dbExecutor.execute {
            localDB.spendsDao().addSpends(spends
                    .map { it.toSpend(localIdCounter.getNext()).toSpendTable() }
            )
        }
    }

    override fun editSpend(spend: Spend) {
        dbExecutor.execute { localDB.spendsDao().editSpend(spend.toSpendTable()) }
    }

    override fun removeSpend(spendId: Long) {
        dbExecutor.execute { localDB.spendsDao().deleteSpend(spendId) }
    }

    override fun removeAllSpends() {
        dbExecutor.execute { localDB.spendsDao().deleteAllSpends() }
    }

    override fun getRecordsList(): LiveData<List<RecordResult>> = localDB.spendsDao().getSpendsAndProfits()

    override fun locallyCreatedSpends(): LiveData<Spend> = locallyCreatedSpends

    @WorkerThread
    override fun getDumpText(): String = localDB.spendsDao()
            .getAllSpendsList()
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

    override fun get30DaysBalance(): LiveData<Long> = combineLatest(
            localDB.profitsDao().get30DaysSum(),
            localDB.spendsDao().get30DaysSum(),
            { profits, spends -> (profits ?: 0L) - (spends ?: 0L) }
    ).map { it ?: 0 }
}