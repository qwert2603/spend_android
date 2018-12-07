package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

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

fun List<Record>.toRecordItemsList(showInfo: ShowInfo): List<RecordsListItem> {
    val currentTimeMillis = System.currentTimeMillis()

    var spendsCount = 0
    var spendsSum = 0L
    var profitsCount = 0
    var profitsSum = 0L

    var daySpendsSum = 0L
    var dayProfitsSum = 0L
    var daySpendsCount = 0
    var dayProfitsCount = 0

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    (0..this.lastIndex + 1).forEach { index ->
        // FAKE_RECORD is needed to add DaySum for earliest real RecordResult in list.
        val record = this.getOrNull(index) ?: FAKE_RECORD
        if (index > 0 && showInfo.showSums) {
            if (this[index - 1].date != record.date
                    && (showInfo.showSpends && daySpendsCount > 0 || showInfo.showProfits && dayProfitsCount > 0)
            ) {
                result.add(DaySum(
                        day = this[index - 1].date,
                        showSpends = showInfo.showSpends,
                        showProfits = showInfo.showProfits,
                        spends = daySpendsSum,
                        profits = dayProfitsSum
                ))
                daySpendsSum = 0L
                dayProfitsSum = 0L
                daySpendsCount = 0
                dayProfitsCount = 0
            }
        }
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
                if (showInfo.showSpends && (!record.isDeleted() || showInfo.showDeleted())) {
                    result.add(record)
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
                if (showInfo.showProfits && (!record.isDeleted() || showInfo.showDeleted())) {
                    result.add(record)
                }
                if (!record.isDeleted() || showInfo.showDeleted()) {
                    ++dayProfitsCount
                }
            }
        }
    }
    result.add(Totals(
            showSpends = showInfo.showSpends,
            showProfits = showInfo.showProfits,
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum,
            totalBalance = profitsSum - spendsSum
    ))

    LogUtils.d("timing_ List<Record>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}


fun sumsInfoChanges(
        longSumPeriodDaysChanges: Observable<Int>,
        shortSumPeriodMinutesChanges: Observable<Int>,
        showInfoChanges: Observable<ShowInfo>,
        recordsListInteractor: RecordsListInteractor
): Observable<SumsInfo> = Observable
        .combineLatest(
                longSumPeriodDaysChanges,
                shortSumPeriodMinutesChanges,
                showInfoChanges,
                RxUtils.minuteChanges().startWith(Any()),
                makeQuad()
        )
        .switchMap { (longSumPeriodDays, shortSumPeriodMinutes, showInfo) ->
            val longSumChanges = Observable
                    .combineLatest(
                            if (showInfo.showSpends) {
                                recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_SPEND, longSumPeriodDays)
                            } else {
                                Observable.just(0L)
                            },
                            if (showInfo.showProfits) {
                                recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_PROFIT, longSumPeriodDays)
                            } else {
                                Observable.just(0L)
                            },
                            BiFunction { s: Long, p: Long -> p - s }
                    )
            val shortSumChanges = Observable
                    .combineLatest(
                            if (showInfo.showSpends) {
                                recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_SPEND, shortSumPeriodMinutes)
                            } else {
                                Observable.just(0L)
                            },
                            if (showInfo.showProfits) {
                                recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_PROFIT, shortSumPeriodMinutes)
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
                            if (longSumPeriodDays > 0) longSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            if (shortSumPeriodMinutes > 0) shortSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            if (showInfo.showChangeKinds) changesCountChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                            Function3 { longSum: Wrapper<out Long>, shortSum: Wrapper<out Long>, changesCount: Wrapper<out Int> ->
                                SumsInfo(longSum.t, shortSum.t, changesCount.t)
                            }
                    )
        }