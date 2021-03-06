package com.qwert2603.spend.sums

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.DateUtils
import java.util.*

fun List<Record>.toSumsList(sumsShowInfo: SumsShowInfo): List<RecordsListItem> {
    val records: List<Record> = this.filter { !it.isDeleted() }

    val currentTimeMillis = System.currentTimeMillis()

    if (records.isEmpty()) return listOf(Totals(
            showSpends = true,
            showProfits = true,
            spendsCount = 0,
            spendsSum = 0L,
            profitsCount = 0,
            profitsSum = 0L
    ))

    val result = ArrayList<RecordsListItem>(records.size * 3)

    val calendar = maxOf(records.first().date, DateUtils.getNow().first).toDateCalendar()

    var spendsCount = 0
    var spendsSum = 0L
    var profitsCount = 0
    var profitsSum = 0L

    var monthSpendsSum = 0L
    var monthProfitsSum = 0L
    var yearSpendsSum = 0L
    var yearProfitsSum = 0L

    var date = calendar.toSDate()
    var index = 0
    var record: Record? = records.first()

    while (date >= records.last().date) {
        var daySpendsSum = 0L
        var dayProfitsSum = 0L

        while (record?.date == date) {
            if (!record.isDeleted()) {
                when (record.recordCategory.recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> {
                        daySpendsSum += record.value
                        monthSpendsSum += record.value
                        yearSpendsSum += record.value
                        ++spendsCount
                        spendsSum += record.value
                    }
                    Const.RECORD_TYPE_ID_PROFIT -> {
                        dayProfitsSum += record.value
                        monthProfitsSum += record.value
                        yearProfitsSum += record.value
                        ++profitsCount
                        profitsSum += record.value
                    }
                }
            }

            ++index
            record = records.getOrNull(index)
        }

        if (sumsShowInfo.showDaySums) {
            result.add(DaySum(
                    day = date,
                    showSpends = true,
                    showProfits = true,
                    spends = daySpendsSum,
                    profits = dayProfitsSum
            ))
        }

        val prevDate = date
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        date = calendar.toSDate()

        // record == null means end of list, so we need to add sums for last month / year.

        if (sumsShowInfo.showMonthSums && (!date.isSameMonth(prevDate) || record == null)) {
            result.add(MonthSum(
                    month = prevDate.date / 100,
                    showSpends = true,
                    showProfits = true,
                    spends = monthSpendsSum,
                    profits = monthProfitsSum
            ))
            monthSpendsSum = 0
            monthProfitsSum = 0
        }
        if (sumsShowInfo.showYearSums && (!date.isSameYear(prevDate) || record == null)) {
            result.add(YearSum(
                    year = prevDate.date / (100 * 100),
                    showSpends = true,
                    showProfits = true,
                    spends = yearSpendsSum,
                    profits = yearProfitsSum
            ))
            yearSpendsSum = 0
            yearProfitsSum = 0
        }
    }

    result.add(Totals(
            showSpends = true,
            showProfits = true,
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum
    ))

    LogUtils.d("timing_ List<Record>.toSumsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}