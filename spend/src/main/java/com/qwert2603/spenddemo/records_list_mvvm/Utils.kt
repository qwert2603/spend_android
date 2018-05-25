package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.daysEqual
import com.qwert2603.spenddemo.utils.monthsEqual
import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.onlyMonth
import java.util.*

fun List<RecordResult>.toRecordItemsList(showInfo: RecordsListViewModel.ShowInfo): List<RecordsListItem> {
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
            if (showInfo.showDateSums
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> true
                        showInfo.showSpends -> daySpendsSum > 0
                        else -> dayProfitsSum > 0
                    }
                    && !calendarPrev.daysEqual(calendarIndex)
            ) {
                result.add(DateSumUI(
                        date = calendarPrev.onlyDate(),
                        showSpends = showInfo.showSpendSum(),
                        showProfits = showInfo.showProfitSum(),
                        spends = daySpendsSum,
                        profits = dayProfitsSum
                ))
                daySpendsSum = 0L
                dayProfitsSum = 0L
            }
            if (showInfo.showMonthSums
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> true
                        showInfo.showSpends -> monthSpendsSum > 0
                        else -> monthProfitsSum > 0
                    }
                    && !calendarPrev.monthsEqual(calendarIndex)
            ) {
                result.add(MonthSumUI(
                        date = calendarPrev.onlyMonth(),
                        showSpends = showInfo.showSpendSum(),
                        showProfits = showInfo.showProfitSum(),
                        spends = monthSpendsSum,
                        profits = monthProfitsSum
                ))
                monthSpendsSum = 0L
                monthProfitsSum = 0L
            }
        }
        calendarPrev.time = calendarIndex.time
        when (tableRow.type) {
            RecordResult.TYPE_SPEND -> {
                daySpendsSum += tableRow.value
                monthSpendsSum += tableRow.value
                ++spendsCount
                spendsSum += tableRow.value
                if (showInfo.showSpends) result.add(SpendUI(tableRow.id, tableRow.kind, tableRow.value, tableRow.date, SyncStatus.REMOTE, null))
            }
            RecordResult.TYPE_PROFIT -> {
                dayProfitsSum += tableRow.value
                monthProfitsSum += tableRow.value
                ++profitsCount
                profitsSum += tableRow.value
                if (showInfo.showProfits) result.add(ProfitUI(tableRow.id, tableRow.kind, tableRow.value, tableRow.date))
            }
        }
    }
    if (this.isNotEmpty()) {
        if (showInfo.showDateSums
                && when {
                    showInfo.showSpends == showInfo.showProfits -> true
                    showInfo.showSpends -> daySpendsSum > 0
                    else -> dayProfitsSum > 0
                }
        ) {
            result.add(DateSumUI(
                    date = calendarPrev.onlyDate(),
                    showSpends = showInfo.showSpendSum(),
                    showProfits = showInfo.showProfitSum(),
                    spends = daySpendsSum,
                    profits = dayProfitsSum
            ))
        }
        if (showInfo.showMonthSums
                && when {
                    showInfo.showSpends == showInfo.showProfits -> true
                    showInfo.showSpends -> monthSpendsSum > 0
                    else -> monthProfitsSum > 0
                }
        ) {
            result.add(MonthSumUI(
                    date = calendarPrev.onlyMonth(),
                    showSpends = showInfo.showSpendSum(),
                    showProfits = showInfo.showProfitSum(),
                    spends = monthSpendsSum,
                    profits = monthProfitsSum
            ))
        }
    }
    result.add(TotalsUI(showInfo.showSpends, showInfo.showProfits, spendsCount, spendsSum, profitsCount, profitsSum, profitsSum - spendsSum))

    LogUtils.d("List<RecordResult>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}