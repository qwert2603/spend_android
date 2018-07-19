package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.navigation.ScreenKey
import com.qwert2603.spenddemo.records_list_mvvm.entity.RecordsListItem
import com.qwert2603.spenddemo.utils.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.coroutines.experimental.asReference
import ru.terrakok.cicerone.Router
import java.util.*
import java.util.concurrent.TimeUnit

class RecordsListViewModel(
        private val spendsRepo: SpendsRepo,
        private val profitsRepo: ProfitsRepo,
        private val userSettingsRepo: UserSettingsRepo,
        private val router: Router
) : ViewModel() {

    val showSpends = MutableLiveData<Boolean>()
    val showProfits = MutableLiveData<Boolean>()
    val showDateSums = MutableLiveData<Boolean>()
    val showMonthSums = MutableLiveData<Boolean>()
    val showIds = MutableLiveData<Boolean>()
    val showChangeKinds = MutableLiveData<Boolean>()
    val longSumPeriodDays = MutableLiveData<Int>()
    val shortSumPeriodMinutes = MutableLiveData<Int>()
    val showTimes = MutableLiveData<Boolean>()

    val sendRecords = SingleLiveEvent<String>()

    private val minuteChangesEvents = SingleLiveEvent<Unit>()
    private val dayChangesEvents = SingleLiveEvent<Unit>()
    private val timeChangesJob: Job

    init {
        showSpends.value = userSettingsRepo.showSpends
        showProfits.value = userSettingsRepo.showProfits
        showDateSums.value = userSettingsRepo.showDateSums
        showMonthSums.value = userSettingsRepo.showMonthSums
        showIds.value = userSettingsRepo.showIds
        showChangeKinds.value = userSettingsRepo.showChangeKinds
        longSumPeriodDays.value = userSettingsRepo.longSumPeriodDays
        shortSumPeriodMinutes.value = userSettingsRepo.shortSumPeriodMinutes
        showTimes.value = userSettingsRepo.showTimes

        minuteChangesEvents.value = Unit
        dayChangesEvents.value = Unit
        timeChangesJob = launch(CommonPool) {
            var prevCalendar = Calendar.getInstance()
            while (isActive) {
                delay(300, TimeUnit.MILLISECONDS)
                val currentCalendar = Calendar.getInstance()
                if (currentCalendar.minute != prevCalendar.minute) launch(UI) { minuteChangesEvents.value = Unit }
                if (currentCalendar.day != prevCalendar.day) launch(UI) { dayChangesEvents.value = Unit }
                prevCalendar = currentCalendar
            }
        }
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

    var pendingMovedSpendId: Long? = null
    var pendingMovedProfitId: Long? = null

    val recordsLiveData: LiveData<Pair<List<RecordsListItem>, FastDiffUtils.FastDiffResult>> = showInfo
            .switchMap { showInfo -> recordsList.map { it to showInfo } }
            .mapBG(object : Mapper<Pair<List<RecordResult>, ShowInfo>, Pair<List<RecordsListItem>, FastDiffUtils.FastDiffResult>> {
                private var prev: List<RecordsListItem>? = null
                override fun invoke(t: Pair<List<RecordResult>, ShowInfo>): Pair<List<RecordsListItem>, FastDiffUtils.FastDiffResult> {
                    val recordItemsList = t.first.toRecordItemsList(t.second)
                    val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                            oldList = prev ?: emptyList(),
                            newList = recordItemsList,
                            id = { this.idInList() },
                            compareOrder = { r1, r2 ->
                                return@fastCalculateDiff r2.time().compareTo(r1.time())
                                        .takeIf { it != 0 }
                                        ?: r2.priority().compareTo(r1.priority())
                                                .takeIf { it != 0 }
                                        ?: r2.id.compareTo(r1.id)
                            },
                            isEqual = { r1, r2 -> r1 == r2 },
                            possiblyMovedItemIds = listOfNotNull(
                                    pendingMovedSpendId?.plus(RecordsListItem.ADDENDUM_ID_SPEND),
                                    pendingMovedProfitId?.plus(RecordsListItem.ADDENDUM_ID_PROFIT)
                            )
                    )
                    pendingMovedSpendId = null
                    pendingMovedProfitId = null
                    prev = recordItemsList

                    return recordItemsList to fastDiffResult
                }
            })

    val redrawAllRecords = dayChangesEvents

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

    fun setLongSumPeriodDays(days: Int) {
        longSumPeriodDays.value = days
        userSettingsRepo.longSumPeriodDays = days
    }

    fun setShortSumPeriodMinutes(minutes: Int) {
        shortSumPeriodMinutes.value = minutes
        userSettingsRepo.shortSumPeriodMinutes = minutes
    }

    fun showTimes(show: Boolean) {
        showTimes.value = show
        userSettingsRepo.showTimes = show
    }

    fun addStubSpends() {
        val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
        val random = Random()
        spendsRepo.addSpends((1..200)
                .map {
                    CreatingSpend(
                            kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                            value = random.nextInt(1000) + 1,
                            date = Date().secondsToZero() - (random.nextInt(2100)).days
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
                            date = Date().secondsToZero() - (random.nextInt(2100)).days
                    )
                }
        )
    }

    fun clearAll() {
        spendsRepo.removeAllSpends()
        profitsRepo.removeAllProfits()
    }

    data class SumsInfo(
            val changesCount: Int,
            val longPeriodDays: Int,
            val longPeriodSum: Long,
            val shortPeriodMinutes: Int,
            val shortPeriodSum: Long
    )

    val sumsInfo: LiveData<SumsInfo> = combineLatest(longSumPeriodDays, shortSumPeriodMinutes, ::Pair)
            .switchMap { (longPeriodDays, shortPeriodMinutes) ->
                val changesCount = combineLatest(listOf(showProfits, showSpends))
                        .switchMap {
                            val showProfits = it[0] == true
                            val showSpends = it[1] == true
                            combineLatest(
                                    liveDataT = if (showProfits || !showSpends) {
                                        profitsRepo.getChangesCount()
                                    } else {
                                        LDUtils.just(0)
                                    },
                                    liveDataU = if (showSpends || !showProfits) {
                                        spendsRepo.getChangesCount()
                                    } else {
                                        LDUtils.just(0)
                                    },
                                    combiner = { p, s -> p + s }
                            )
                        }
                val longSum = if (longPeriodDays > 0) {
                    dayChangesEvents.switchMap {
                        combineLatest(listOf(showProfits, showSpends))
                                .switchMap {
                                    val showProfits = it[0] == true
                                    val showSpends = it[1] == true
                                    combineLatest(
                                            liveDataT = if (showProfits || !showSpends) {
                                                profitsRepo.getSumLastDays(longPeriodDays)
                                            } else {
                                                LDUtils.just(0L)
                                            },
                                            liveDataU = if (showSpends || !showProfits) {
                                                spendsRepo.getSumLastDays(longPeriodDays)
                                            } else {
                                                LDUtils.just(0L)
                                            },
                                            combiner = { p, s -> p - s }
                                    )
                                }
                    }
                } else {
                    LDUtils.just(0L)
                }
                val shortSum = if (shortPeriodMinutes > 0) {
                    minuteChangesEvents.switchMap {
                        combineLatest(listOf(showProfits, showSpends))
                                .switchMap {
                                    val showProfits = it[0] == true
                                    val showSpends = it[1] == true
                                    combineLatest(
                                            liveDataT = if (showProfits || !showSpends) {
                                                profitsRepo.getSumLastMinutes(shortPeriodMinutes)
                                            } else {
                                                LDUtils.just(0L)
                                            },
                                            liveDataU = if (showSpends || !showProfits) {
                                                spendsRepo.getSumLastMinutes(shortPeriodMinutes)
                                            } else {
                                                LDUtils.just(0L)
                                            },
                                            combiner = { p, s -> p - s }
                                    )
                                }
                    }
                } else {
                    LDUtils.just(0L)
                }
                combineLatest(listOf(changesCount.map { it.toLong() }, longSum, shortSum))
                        .map {
                            SumsInfo(
                                    changesCount = it[0].toInt(),
                                    longPeriodDays = longPeriodDays,
                                    longPeriodSum = it[1],
                                    shortPeriodMinutes = shortPeriodMinutes,
                                    shortPeriodSum = it[2]
                            )
                        }
            }

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
        pendingMovedSpendId = spend.id
        spendsRepo.editSpend(spend)
    }

    fun editProfit(profit: Profit) {
        pendingMovedProfitId = profit.id
        profitsRepo.editProfit(profit)
    }

    fun sendRecords() {
        val vmRef = asReference()
        launch(UI) {
            vmRef().creatingRecordsText.value = Unit
            val spends = async(CommonPool) { spendsRepo.getDumpText() }
            val profits = async(CommonPool) { profitsRepo.getDumpText() }
            vmRef().sendRecords.value = "SPENDS:\n${spends.await()}\n\nPROFITS:\n${profits.await()}"
        }
    }

    fun moveToChangesList() {
        router.navigateTo(ScreenKey.CHANGES_LIST.name)
    }

    val createdSpendsEvents: SingleLiveEvent<Spend> = spendsRepo.locallyCreatedSpends()

    val syncingItemIdsInList: LiveData<Set<Long>> = combineLatest(
            liveDataT = spendsRepo.syncingSpendIds()
                    .map { it.map { it + RecordsListItem.ADDENDUM_ID_SPEND }.toSet() },
            liveDataU = profitsRepo.syncingProfitIds()
                    .map { it.map { it + RecordsListItem.ADDENDUM_ID_PROFIT }.toSet() },
            startT = emptySet(),
            startU = emptySet(),
            combiner = { s, p -> s + p }
    )

    val createdProfitsEvents: SingleLiveEvent<Profit> = profitsRepo.locallyCreatedProfits()

    val creatingRecordsText = SingleLiveEvent<Unit>()
}