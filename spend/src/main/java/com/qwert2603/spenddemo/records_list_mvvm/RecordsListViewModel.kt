package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem
import com.qwert2603.spenddemo.utils.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RecordsListViewModel(
        private val localDB: LocalDB,
        private val userSettingsRepo: UserSettingsRepo
) : ViewModel() {

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
        showSpends.value = userSettingsRepo.showSpends
        showProfits.value = userSettingsRepo.showProfits
        showDateSums.value = userSettingsRepo.showDateSums
        showMonthSums.value = userSettingsRepo.showMonthSums
    }

    data class ShowInfo(
            val showSpends: Boolean,
            val showProfits: Boolean,
            val showDateSums: Boolean,
            val showMonthSums: Boolean
    ) {
        fun showSpendSum() = showSpends || !showProfits
        fun showProfitSum() = showProfits || !showSpends
        fun showSpendsEnable() = showProfits || showDateSums || showMonthSums
        fun showProfitsEnable() = showSpends || showDateSums || showMonthSums
        fun showDateSumsEnable() = showSpends || showProfits || showMonthSums
        fun showMonthSumsEnable() = showSpends || showProfits || showDateSums
        fun newProfitEnable() = showProfits
        fun newSpendVisible() = showSpends
        fun showFloatingDate() = showDateSums && (showSpends || showProfits)
    }

    val showInfo = combineLatest(listOf(showSpends, showProfits, showDateSums, showMonthSums))
            .map {
                ShowInfo(
                        showSpends = it[0] == true,
                        showProfits = it[1] == true,
                        showDateSums = it[2] == true,
                        showMonthSums = it[3] == true
                )
            }

    private val recordsList = localDB.spendsDao().getSpendsAndProfits()

    val recordsLiveData: LiveData<List<RecordsListItem>> = showInfo
            .switchMap { showInfo ->
                recordsList
                        .map {
                            // todo: background thread coroutines.
                            it.toRecordItemsList(showInfo)
                        }
            }

    fun showSpends(show: Boolean) {
        showSpends.value = show
        userSettingsRepo.showSpends = show
    }

    fun showProfits(show: Boolean) {
        showProfits.value = show
        userSettingsRepo.showProfits = show
    }

    fun showDateSums(show: Boolean) {
        showDateSums.value = show
        userSettingsRepo.showDateSums = show
    }

    fun showMonthSums(show: Boolean) {
        showMonthSums.value = show
        userSettingsRepo.showMonthSums = show
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

    val balance30Days = combineLatest(
            localDB.profitsDao().get30DaysSum(),
            localDB.spendsDao().get30DaysSum(),
            { profits, spends -> (profits ?: 0L) - (spends ?: 0L) }
    )
}