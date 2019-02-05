package com.qwert2603.spend.records_list

import com.google.firebase.perf.metrics.AddTrace
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.util.*
import java.util.concurrent.TimeUnit

private val FAKE_RECORD = Record(
        uuid = "FAKE_RECORD",
        recordCategory = RecordCategory(
                "FAKE_CATEGORY",
                Const.RECORD_TYPE_ID_SPEND,
                "nth"
        ),
        date = SDate(0),
        time = null,
        kind = "nth",
        value = 1,
        change = null
)

@AddTrace(name = "toRecordItemsList")
fun List<Record>.toRecordItemsList(
        showInfo: ShowInfo,
        sortByValue: Boolean,
        showFilters: Boolean,
        longSumPeriod: Days,
        shortSumPeriod: Minutes,
        recordsFilters: RecordsFilters,
        selectedRecordsUuids: HashSet<String>
): List<RecordsListItem> {

    val currentTimeMillis = System.currentTimeMillis()

    val calendarL = GregorianCalendar.getInstance()
    val calendarS = GregorianCalendar.getInstance().also { it.timeInMillis = calendarL.timeInMillis }

    calendarL.add(Calendar.DAY_OF_MONTH, -longSumPeriod.days + 1)
    calendarS.add(Calendar.MINUTE, -shortSumPeriod.minutes + 1)

    val longSumBound = calendarL.toSDate()
    val shortSumBound = calendarS.toSDate() to calendarS.toSTime()

    val shortPeriodDivider = PeriodDivider(
            date = shortSumBound.first,
            time = shortSumBound.second,
            interval = shortSumPeriod
    )

    val longPeriodDivider = PeriodDivider(
            date = longSumBound,
            time = null,
            interval = longSumPeriod
    )

    LogUtils.d { "toRecordItemsList shortPeriodDivider=$shortPeriodDivider" }
    LogUtils.d { "toRecordItemsList longPeriodDivider=$longPeriodDivider" }

    var spendsCount = 0
    var spendsSum = 0L
    var profitsCount = 0
    var profitsSum = 0L

    var daySpendsSum = 0L
    var dayProfitsSum = 0L
    var daySpendsCount = 0
    var dayProfitsCount = 0

    // don't add divider if interval == 0.
    var needAddLongSumDivider = !sortByValue && !showFilters && longSumPeriod.days > 0
    var needAddShortSumDivider = !sortByValue && !showFilters && shortSumPeriod.minutes > 0

    var atLeastOneRecordAdded = false

    val dateMultiplier = 100L * 100L
    fun RecordsListItem.datePlusTime() = when (this) {
        is Record -> date.date * dateMultiplier + (time?.time ?: 0)
        is DaySum -> day.date * dateMultiplier
        is PeriodDivider -> date.date * dateMultiplier + (time?.time ?: 0)
        else -> null!!
    }

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    (0..this.lastIndex + 1).forEach { index ->
        // FAKE_RECORD is needed to add DaySum for earliest real RecordResult in list.
        val record = this.getOrNull(index) ?: FAKE_RECORD
        if (!sortByValue && !showFilters && index > 0 && showInfo.showSums) {
            if (this[index - 1].date != record.date
                    && (showInfo.showSpends && daySpendsCount > 0 || showInfo.showProfits && dayProfitsCount > 0)
            ) {
                val daySum = DaySum(
                        day = this[index - 1].date,
                        showSpends = showInfo.showSpends,
                        showProfits = showInfo.showProfits,
                        spends = daySpendsSum,
                        profits = dayProfitsSum
                )

                if (needAddShortSumDivider && daySum.datePlusTime() < shortPeriodDivider.datePlusTime()) {
                    needAddShortSumDivider = false
                    if (atLeastOneRecordAdded) {
                        result.add(shortPeriodDivider)
                    }
                }

                result.add(daySum)
                daySpendsSum = 0L
                dayProfitsSum = 0L
                daySpendsCount = 0
                dayProfitsCount = 0
            }
        }

        if (needAddShortSumDivider && record.datePlusTime() < shortPeriodDivider.datePlusTime()) {
            needAddShortSumDivider = false
            if (atLeastOneRecordAdded) {
                result.add(shortPeriodDivider)
            }
        }
        if (needAddLongSumDivider && record.date < longSumBound) {
            needAddLongSumDivider = false
            if (atLeastOneRecordAdded) {
                result.add(longPeriodDivider)
            }
        }

        val recordIsSelected = record.uuid in selectedRecordsUuids
        if (recordIsSelected || recordsFilters.check(record)) {
            when {
                record == FAKE_RECORD -> {
                    // nth
                }
                record.recordCategory.recordTypeId == Const.RECORD_TYPE_ID_SPEND -> {
                    if (!record.isDeleted()) {
                        daySpendsSum += record.value
                        ++spendsCount
                        spendsSum += record.value
                    }
                    if (recordIsSelected || (showInfo.showSpends && (!record.isDeleted() || showInfo.showDeleted()))) {
                        result.add(record)
                        atLeastOneRecordAdded = true
                    }
                    if (!record.isDeleted() || showInfo.showDeleted()) {
                        ++daySpendsCount
                    }
                }
                record.recordCategory.recordTypeId == Const.RECORD_TYPE_ID_PROFIT -> {
                    if (!record.isDeleted()) {
                        dayProfitsSum += record.value
                        ++profitsCount
                        profitsSum += record.value
                    }
                    if (recordIsSelected || (showInfo.showProfits && (!record.isDeleted() || showInfo.showDeleted()))) {
                        result.add(record)
                        atLeastOneRecordAdded = true
                    }
                    if (!record.isDeleted() || showInfo.showDeleted()) {
                        ++dayProfitsCount
                    }
                }
            }
        }
    }

    if (sortByValue) {
        // sort is stable
        result.sortByDescending { (it as Record).value }
    }

    result.add(Totals(
            showSpends = showInfo.showSpends,
            showProfits = showInfo.showProfits,
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum
    ))

    LogUtils.d("timing_ List<Record>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}


fun sumsInfoChanges(
        longSumPeriodChanges: Observable<Days>,
        shortSumPeriodChanges: Observable<Minutes>,
        showInfoChanges: Observable<ShowInfo>,
        recordsFiltersChanges: Observable<RecordsFilters>,
        recordsListInteractor: RecordsListInteractor
): Observable<SumsInfo> = Observable
        .combineLatest(
                longSumPeriodChanges,
                shortSumPeriodChanges,
                showInfoChanges,
                recordsFiltersChanges,
                RxUtils.minuteChanges().startWith(Any()),
                makeQuint()
        )
        .switchMap { (longSumPeriodDays, shortSumPeriodMinutes, showInfo, recordsFilters) ->
            val longSumChanges = Observable
                    .combineLatest(
                            if (showInfo.showSpends) {
                                recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_SPEND, longSumPeriodDays, recordsFilters)
                            } else {
                                Observable.just(0L)
                            },
                            if (showInfo.showProfits) {
                                recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_PROFIT, longSumPeriodDays, recordsFilters)
                            } else {
                                Observable.just(0L)
                            },
                            BiFunction { s: Long, p: Long -> p - s }
                    )
            val shortSumChanges = Observable
                    .combineLatest(
                            if (showInfo.showSpends) {
                                recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_SPEND, shortSumPeriodMinutes, recordsFilters)
                            } else {
                                Observable.just(0L)
                            },
                            if (showInfo.showProfits) {
                                recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_PROFIT, shortSumPeriodMinutes, recordsFilters)
                            } else {
                                Observable.just(0L)
                            },
                            BiFunction { s: Long, p: Long -> p - s }
                    )
            val changesCountChanges = recordsListInteractor
                    .getLocalChangesCount(listOfNotNull(
                            Const.RECORD_TYPE_ID_SPEND.takeIf { showInfo.showSpends },
                            Const.RECORD_TYPE_ID_PROFIT.takeIf { showInfo.showProfits }
                    ))

            Observable
                    .combineLatest(
                            if (longSumPeriodDays.days > 0) longSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            if (shortSumPeriodMinutes.minutes > 0) shortSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            if (showInfo.showChangeKinds) changesCountChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            Function3 { longSum: Wrapper<out Long>, shortSum: Wrapper<out Long>, changesCount: Wrapper<out Int> ->
                                SumsInfo(longSum.t, shortSum.t, changesCount.t)
                            }
                    )
        }

fun Observable<SyncState>.modifyForUi(): Observable<SyncState> = this
        .distinctUntilChanged()
        .switchMapSingle { syncState ->
            Single.just(syncState)
                    .let {
                        when (syncState) {
                            SyncState.Syncing -> it.delay(100, TimeUnit.MILLISECONDS)
                            is SyncState.Synced -> it.delay(300, TimeUnit.MILLISECONDS)
                            is SyncState.Error -> it
                        }
                    }
        }