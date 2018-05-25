package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem
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
    val showDateSums = MutableLiveData<Boolean>()
    val showMonthSums = MutableLiveData<Boolean>()

    init {
        LOCAL_DB = localDB
        showSpends.value = false
        showProfits.value = false
        showDateSums.value = false
        showMonthSums.value = false
    }

    private val recordsList = localDB.spendsDao().getSpendsAndProfits()

    data class RecordListInfo(val showSpends: Boolean, val showProfits: Boolean, val showDateSums: Boolean, val showMonthSums: Boolean)

    val recordsLiveData: LiveData<List<RecordsListItem>> = combineLatest(listOf(showSpends, showProfits, showDateSums, showMonthSums))
            .switchMap { list ->
                val showSpends = list[0] == true
                val showProfits = list[1] == true
                val showDateSums = list[2] == true
                val showMonthSums = list[3] == true
                val showSpendSum = showSpends || !showProfits
                val showProfitSum = showProfits || !showSpends
                recordsList
                        .map {
                            // todo: background thread coroutines.
                            it.toRecordItemsList(
                                    showSpends = showSpends,
                                    showProfits = showProfits,
                                    showDateSums = showDateSums,
                                    showMonthSums = showMonthSums,
                                    showSpendSum = showSpendSum,
                                    showProfitSum = showProfitSum
                            )
                        }
            }

    fun showSpends(show: Boolean) {
        showSpends.value = show
    }

    fun showProfits(show: Boolean) {
        showProfits.value = show
    }

    fun showDateSums(show: Boolean) {
        showDateSums.value = show
    }

    fun showMonthSums(show: Boolean) {
        showMonthSums.value = show
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