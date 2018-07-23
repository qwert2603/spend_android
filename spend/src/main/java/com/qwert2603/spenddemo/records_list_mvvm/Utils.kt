package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.records_list_mvvm.entity.*
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
    var daySpendsCountNotDeleted = 0
    var dayProfitsCountNotDeleted = 0

    var monthSpendsSum = 0L
    var monthProfitsSum = 0L
    var monthSpendsCountNotDeleted = 0
    var monthProfitsCountNotDeleted = 0

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    val calendarPrev = Calendar.getInstance()
    val calendarIndex = Calendar.getInstance()
    this.forEachIndexed { index, tableRow ->
        calendarIndex.time = tableRow.date
        if (index > 0) {
            if (showInfo.showDateSums
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> daySpendsCountNotDeleted > 0 || dayProfitsCountNotDeleted > 0
                        showInfo.showSpends -> daySpendsCountNotDeleted > 0
                        else -> dayProfitsCountNotDeleted > 0
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
                daySpendsCountNotDeleted = 0
                dayProfitsCountNotDeleted = 0
            }
            if (showInfo.showMonthSums
                    && when {
                        showInfo.showSpends == showInfo.showProfits -> monthSpendsCountNotDeleted > 0 || monthProfitsCountNotDeleted > 0
                        showInfo.showSpends -> monthSpendsCountNotDeleted > 0
                        else -> monthProfitsCountNotDeleted > 0
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
                monthSpendsCountNotDeleted = 0
                monthProfitsCountNotDeleted = 0
            }
        }
        calendarPrev.time = calendarIndex.time
        when (tableRow.type) {
            RecordResult.TYPE_SPEND -> {
                if (tableRow.changeKind != ChangeKind.DELETE) {
                    daySpendsSum += tableRow.value
                    monthSpendsSum += tableRow.value
                    ++spendsCount
                    spendsSum += tableRow.value
                }
                if (showInfo.showSpends && (tableRow.changeKind != ChangeKind.DELETE || showInfo.showDeleted)) {
                    result.add(SpendUI(
                            id = tableRow.id,
                            kind = tableRow.kind,
                            value = tableRow.value,
                            date = tableRow.date,
                            changeKind = tableRow.changeKind
                    ))
                }
                if (tableRow.changeKind != ChangeKind.DELETE || showInfo.showDeleted) {
                    ++daySpendsCountNotDeleted
                    ++monthSpendsCountNotDeleted
                }
            }
            RecordResult.TYPE_PROFIT -> {
                if (tableRow.changeKind != ChangeKind.DELETE) {
                    dayProfitsSum += tableRow.value
                    monthProfitsSum += tableRow.value
                    ++profitsCount
                    profitsSum += tableRow.value
                }
                if (showInfo.showProfits && (tableRow.changeKind != ChangeKind.DELETE || showInfo.showDeleted)) {
                    result.add(ProfitUI(
                            id = tableRow.id,
                            kind = tableRow.kind,
                            value = tableRow.value,
                            date = tableRow.date,
                            changeKind = tableRow.changeKind
                    ))
                }
                if (tableRow.changeKind != ChangeKind.DELETE || showInfo.showDeleted) {
                    ++dayProfitsCountNotDeleted
                    ++monthProfitsCountNotDeleted
                }
            }
        }
    }
    if (this.isNotEmpty()) {
        if (showInfo.showDateSums
                && when {
                    showInfo.showSpends == showInfo.showProfits -> daySpendsCountNotDeleted > 0 || dayProfitsCountNotDeleted > 0
                    showInfo.showSpends -> daySpendsCountNotDeleted > 0
                    else -> dayProfitsCountNotDeleted > 0
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
                    showInfo.showSpends == showInfo.showProfits -> monthSpendsCountNotDeleted > 0 || monthProfitsCountNotDeleted > 0
                    showInfo.showSpends -> monthSpendsCountNotDeleted > 0
                    else -> monthProfitsCountNotDeleted > 0
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
    result.add(TotalsUI(
            showSpends = showInfo.showSpendSum(),
            showProfits = showInfo.showProfitSum(),
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum,
            totalBalance = profitsSum - spendsSum
    ))

    LogUtils.d("List<RecordResult>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}