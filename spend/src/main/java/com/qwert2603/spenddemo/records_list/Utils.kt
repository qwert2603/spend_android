package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.sync_processor.IdentifiableString
import com.qwert2603.spenddemo.utils.Const

private val FAKE_RECORD = Record(
        uuid = IdentifiableString.NO_UUID,
        recordTypeId = Const.RECORD_TYPE_ID_SPEND,
        date = 0,
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

    var monthSpendsSum = 0L
    var monthProfitsSum = 0L
    var monthSpendsCount = 0
    var monthProfitsCount = 0

    var yearSpendsSum = 0L
    var yearProfitsSum = 0L
    var yearSpendsCount = 0
    var yearProfitsCount = 0

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    (0..this.lastIndex + 1).forEach { index ->
        // FAKE_RECORD is needed to add DaySum & MonthSum & YearSum for earliest real RecordResult in list.
        val record = this.getOrNull(index) ?: FAKE_RECORD
        if (index > 0 && showInfo.showSums) {
            if (this[index - 1].date != record.date
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> daySpendsCount > 0 || dayProfitsCount > 0
                        showInfo.showSpends -> daySpendsCount > 0
                        else -> dayProfitsCount > 0
                    }
            ) {
                result.add(DaySum(
                        day = this[index - 1].date,
                        showSpends = showInfo.showSpendSum(),
                        showProfits = showInfo.showProfitSum(),
                        spends = daySpendsSum,
                        profits = dayProfitsSum
                ))
                daySpendsSum = 0L
                dayProfitsSum = 0L
                daySpendsCount = 0
                dayProfitsCount = 0
            }
            if (this[index - 1].date / 100 != record.date / 100
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> monthSpendsCount > 0 || monthProfitsCount > 0
                        showInfo.showSpends -> monthSpendsCount > 0
                        else -> monthProfitsCount > 0
                    }
            ) {
                result.add(MonthSum(
                        month = this[index - 1].date / 100,
                        showSpends = showInfo.showSpendSum(),
                        showProfits = showInfo.showProfitSum(),
                        spends = monthSpendsSum,
                        profits = monthProfitsSum
                ))
                monthSpendsSum = 0L
                monthProfitsSum = 0L
                monthSpendsCount = 0
                monthProfitsCount = 0
            }
            if (this[index - 1].date / (100 * 100) != record.date / (100 * 100)
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> yearSpendsCount > 0 || yearProfitsCount > 0
                        showInfo.showSpends -> yearSpendsCount > 0
                        else -> yearProfitsCount > 0
                    }
            ) {
                result.add(YearSum(
                        year = this[index - 1].date / (100 * 100),
                        showSpends = showInfo.showSpendSum(),
                        showProfits = showInfo.showProfitSum(),
                        spends = yearSpendsSum,
                        profits = yearProfitsSum
                ))
                yearSpendsSum = 0L
                yearProfitsSum = 0L
                yearSpendsCount = 0
                yearProfitsCount = 0
            }
        }
        when {
            record == FAKE_RECORD -> {
                // nth
            }
            record.recordTypeId == Const.RECORD_TYPE_ID_SPEND -> {
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE) {
                    daySpendsSum += record.value
                    monthSpendsSum += record.value
                    yearSpendsSum += record.value
                    ++spendsCount
                    spendsSum += record.value
                }
                if (showInfo.showSpends && (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted())) {
                    result.add(record)
                }
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted()) {
                    ++daySpendsCount
                    ++monthSpendsCount
                    ++yearSpendsCount
                }
            }
            record.recordTypeId == Const.RECORD_TYPE_ID_PROFIT -> {
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE) {
                    dayProfitsSum += record.value
                    monthProfitsSum += record.value
                    yearProfitsSum += record.value
                    ++profitsCount
                    profitsSum += record.value
                }
                if (showInfo.showProfits && (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted())) {
                    result.add(record)
                }
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted()) {
                    ++dayProfitsCount
                    ++monthProfitsCount
                    ++yearProfitsCount
                }
            }
        }
    }
    result.add(Totals(
            showSpends = showInfo.showSpendSum(),
            showProfits = showInfo.showProfitSum(),
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum,
            totalBalance = profitsSum - spendsSum
    ))

    LogUtils.d("timing_ List<Record>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}