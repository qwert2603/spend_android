package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.daysEqual
import com.qwert2603.spenddemo.utils.monthsEqual
import java.util.*

fun List<RecordResult>.toRecordItemsList(
        showSpends: Boolean,
        showProfits: Boolean,
        showDateSums: Boolean,
        showMonthSums: Boolean,
        showSpendSum: Boolean,
        showProfitSum: Boolean
): List<RecordsListItem> {
    val currentTimeMillis = System.currentTimeMillis()

    var spendsCount = 0
    var spendsSum = 0L
    var profitsCount = 0
    var profitsSum = 0L

    var daySpendsSum = 0L
    var dayProfitsSum = 0L

    var monthSpendsSum = 0L
    var monthProfitsSum = 0L

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    val calendarPrev = Calendar.getInstance()
    val calendarIndex = Calendar.getInstance()
    this.forEachIndexed { index, tableRow ->
        calendarIndex.time = tableRow.date
        if (index > 0) {
            if (showDateSums) {
                if (!calendarPrev.daysEqual(calendarIndex)) {
                    result.add(DateSumUI(calendarPrev.time, showSpendSum, showProfitSum, daySpendsSum, dayProfitsSum))
                    daySpendsSum = 0L
                    dayProfitsSum = 0L
                }
            }
            if (showMonthSums) {
                if (!calendarPrev.monthsEqual(calendarIndex)) {
                    result.add(MonthSumUI(calendarPrev.time, showSpendSum, showProfitSum, monthSpendsSum, monthProfitsSum))
                    monthSpendsSum = 0L
                    monthProfitsSum = 0L
                }
            }
        }
        calendarPrev.time = calendarIndex.time
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
    val dateInPrev = (this.lastOrNull())?.date
    if (dateInPrev != null) {
        if (showDateSums) result.add(DateSumUI(dateInPrev, showSpendSum, showProfitSum, daySpendsSum, dayProfitsSum))
        if (showMonthSums) result.add(MonthSumUI(dateInPrev, showSpendSum, showProfitSum, monthSpendsSum, monthProfitsSum))
    }
    result.add(TotalsUI(showSpends, showProfits, spendsCount, spendsSum, profitsCount, profitsSum, profitsSum - spendsSum))

    LogUtils.d("List<RecordResult>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}