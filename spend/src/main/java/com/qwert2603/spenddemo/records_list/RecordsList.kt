package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.records_list.entity.*

// todo: use this in presenter for quick state reducing.
class RecordsContainer(
        val months: List<MonthContainer>,
        val totalsUI: TotalsUI
) {
    val size = 1 + months.sumBy { it.size }
}

class MonthContainer(
        val monthSumUI: MonthSumUI,
        val days: List<DayContainer>
) {
    val size = 1 + days.sumBy { it.size }
}

class DayContainer(
        val dateSumUI: DateSumUI,
        val spends: List<SpendUI>,
        val profits: List<ProfitUI>
) {
    val size = 1 + spends.size + profits.size
    operator fun get(i: Int) = when (i) {
        0 -> dateSumUI
        in 1..spends.size + 1 -> spends[i - 1]
        else -> profits[i - spends.size - 1]
    }
}