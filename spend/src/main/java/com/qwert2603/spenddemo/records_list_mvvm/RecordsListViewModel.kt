package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.records_list_mvvm.entity.RecordsListItem
import com.qwert2603.spenddemo.utils.*
import java.util.*
import java.util.concurrent.Executor

class RecordsListViewModel(
        private val spendsRepo: SpendsRepo,
        private val profitsRepo: ProfitsRepo,
        private val userSettingsRepo: UserSettingsRepo,
        private val backgroundExecutor: Executor
) : ViewModel() {

    val showSpends = MutableLiveData<Boolean>()
    val showProfits = MutableLiveData<Boolean>()
    val showDateSums = MutableLiveData<Boolean>()
    val showMonthSums = MutableLiveData<Boolean>()
    val showIds = MutableLiveData<Boolean>()
    val showChangeKinds = MutableLiveData<Boolean>()

    val sendRecords = SingleLiveEvent<String>()

    init {
        showSpends.value = userSettingsRepo.showSpends
        showProfits.value = userSettingsRepo.showProfits
        showDateSums.value = userSettingsRepo.showDateSums
        showMonthSums.value = userSettingsRepo.showMonthSums
        showIds.value = userSettingsRepo.showIds
        showChangeKinds.value = userSettingsRepo.showChangeKinds
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

    private val recordsList = spendsRepo.getRecordsList()

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

    fun showIds(show: Boolean) {
        showIds.value = show
        userSettingsRepo.showIds = show
    }

    fun showChangeKinds(show: Boolean) {
        showChangeKinds.value = show
        userSettingsRepo.showChangeKinds = show
    }

    fun addStubSpends() {
        val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
        val random = Random()
        spendsRepo.addSpends((1..200)
                .map {
                    CreatingSpend(
                            kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                            value = random.nextInt(1000) + 1,
                            date = Date() - (random.nextInt(2100)).days
                    )
                })
    }

    fun addStubProfits() {
        val stubSpendKinds = listOf("стипендия", "зарплата", "аванс", "доход")
        val random = Random()
        profitsRepo.addProfits((1..200)
                .map {
                    CreatingProfit(
                            kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                            value = random.nextInt(10000) + 1,
                            date = Date() - (random.nextInt(2100)).days
                    )
                }
        )
    }

    fun clearAll() {
        spendsRepo.removeAllSpends()
        profitsRepo.removeAllProfits()
    }

    val balance30Days = spendsRepo.get30DaysBalance()

    fun deleteSpend(id: Long) {
        spendsRepo.removeSpend(id)
    }

    fun deleteProfit(id: Long) {
        profitsRepo.removeProfit(id)
    }

    fun addProfit(creatingProfit: CreatingProfit) {
        profitsRepo.addProfit(creatingProfit)
    }

    fun editSpend(spend: Spend) {
        spendsRepo.editSpend(spend)
    }

    fun editProfit(profit: Profit) {
        profitsRepo.editProfit(profit)
    }

    fun sendRecords() {
        backgroundExecutor.execute {
            sendRecords.postValue("SPENDS:\n${spendsRepo.getDumpText()}\n\nPROFITS:\n${profitsRepo.getDumpText()}")
        }
    }
}