package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem
import com.qwert2603.spenddemo.records_list.entity.SpendUI
import com.qwert2603.spenddemo.utils.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RecordsListViewModel(private val localDB: LocalDB) : ViewModel() {

    companion object {
        lateinit var LOCAL_DB: LocalDB
        val EXECUTOR: Executor = Executors.newSingleThreadExecutor()
    }

    val showSpends = MutableLiveData<Boolean>()
    val showProfits = MutableLiveData<Boolean>()

    init {
        LOCAL_DB = localDB
        showSpends.value = false
        showProfits.value = false
    }

    private val recordsList = localDB.spendsDao().getSpendsAndProfits()

    val recordsLiveData: LiveData<Triple<List<RecordsListItem>, Boolean, Boolean>> = combineLatest(showSpends, showProfits, { q, w -> Pair(q, w) })
            .switchMap { (showSpends, showProfits) ->
                recordsList
                        .map {
                            it
                                    .filter {
                                        when (it.type) {
                                            1 -> showSpends
                                            2 -> showProfits
                                            else -> false
                                        }
                                    }
                                    .map {
                                        when (it.type) {
                                            1 -> SpendUI(it.id, it.kind, it.value, it.date, SyncStatus.REMOTE, null)
                                            2 -> ProfitUI(it.id, it.kind, it.value, it.date)
                                            else -> null!!
                                        }
                                    }
                                    .let { Triple(it, showSpends, showProfits) }
                        }
            }

    fun showSpends(show: Boolean) {
        showSpends.value = show
    }

    fun showProfits(show: Boolean) {
        showProfits.value = show
    }

    fun addStubSpends() {
        Executors.newSingleThreadExecutor().execute {
            val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
            val random = Random()
            localDB.spendsDao().addSpends((1..200).map {
                SpendTable(
                        id = random.nextLong(),
                        kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                        value = random.nextInt(1000) + 1,
                        date = Date() - (random.nextInt(2100)).days
                )
            })
        }
    }

    fun addStubProfits() {
        Executors.newSingleThreadExecutor().execute {
            val stubSpendKinds = listOf("стипендия", "зарплата", "аванс", "доход")
            val random = Random()
            localDB.profitsDao().addProfits((1..200).map {
                ProfitTable(
                        id = random.nextLong(),
                        kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                        value = random.nextInt(10000) + 1,
                        date = Date() - (random.nextInt(2100)).days
                )
            })
        }
    }

    fun clearAll() {
        Executors.newSingleThreadExecutor().execute {
            localDB.spendsDao().removeAllSpends()
            localDB.profitsDao().removeAllProfits()
        }
    }

    val recordsCounts = localDB.spendsDao().getCounts()
            .map { "${it[0]} ${it[1]}" }
}