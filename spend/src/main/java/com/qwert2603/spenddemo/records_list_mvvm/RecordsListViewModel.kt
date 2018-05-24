package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.records_list.entity.*
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

    val recordsLiveData: LiveData<Pair<ArrayList<RecordsListItem>, RecordListInfo>> = combineLatest(listOf(showSpends, showProfits, showDateSums, showMonthSums))
            .switchMap { list ->
                val showSpends = list[0] == true
                val showProfits = list[1] == true
                val showDateSums = list[2] == true
                val showMonthSums = list[3] == true
                val showSpendSum = showSpends || !showProfits
                val showProfitSum = showProfits || !showSpends
                val recordListInfo = RecordListInfo(showSpends, showProfits, showDateSums, showMonthSums)
                recordsList
                        .map { tableList ->
                            val currentTimeMillis = System.currentTimeMillis()

                            var spendsCount = 0
                            var spendsSum = 0L
                            var profitsCount = 0
                            var profitsSum = 0L

                            var daySpendsSum = 0L
                            var dayProfitsSum = 0L

                            var monthSpendsSum = 0L
                            var monthProfitsSum = 0L

                            val result = ArrayList<RecordsListItem>(tableList.size * 2)
                            tableList.forEachIndexed { index, tableRow ->
                                if (index > 0) {
                                    // todo: optimize onlyDate/onlyMonth
                                    if (showDateSums) {
                                        val dateInPrev = tableList[index - 1].date.onlyDate()
                                        if (dateInPrev != tableRow.date.onlyDate()) {
                                            result.add(DateSumUI(dateInPrev, showSpendSum, showProfitSum, daySpendsSum, dayProfitsSum))
                                            daySpendsSum = 0L
                                            dayProfitsSum = 0L
                                        }
                                    }
                                    if (showMonthSums) {
                                        val monthInPrev = tableList[index - 1].date.onlyMonth()
                                        if (monthInPrev != tableRow.date.onlyMonth()) {
                                            result.add(MonthSumUI(monthInPrev, showSpendSum, showProfitSum, monthSpendsSum, monthProfitsSum))
                                            monthSpendsSum = 0L
                                            monthProfitsSum = 0L
                                        }
                                    }
                                }
                                when (tableRow.type) {
                                    RecordResult.TYPE_SPEND -> {
                                        daySpendsSum += tableRow.value
                                        monthSpendsSum += tableRow.value
                                        ++spendsCount
                                        spendsSum += tableRow.value
                                        if (showSpends) result.add(SpendUI(tableRow.id, tableRow.kind, tableRow.value, tableRow.date, SyncStatus.REMOTE, null))
                                    }
                                    RecordResult.TYPE_PROFIT -> {
                                        dayProfitsSum += tableRow.value
                                        monthProfitsSum += tableRow.value
                                        ++profitsCount
                                        profitsSum += tableRow.value
                                        if (showProfits) result.add(ProfitUI(tableRow.id, tableRow.kind, tableRow.value, tableRow.date))
                                    }
                                }
                            }
                            val dateInPrev = (tableList.lastOrNull())?.date?.onlyDate()
                            if (dateInPrev != null) {
                                result.add(DateSumUI(dateInPrev, showSpends, showProfits, daySpendsSum, dayProfitsSum))
                            }
                            result.add(TotalsUI(showSpends, showProfits, spendsCount, spendsSum, profitsCount, profitsSum, profitsSum - spendsSum))

                            LogUtils.d("RecordsListViewModel recordsLiveData map ${System.currentTimeMillis() - currentTimeMillis} ms")

                            Pair(result, recordListInfo)
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