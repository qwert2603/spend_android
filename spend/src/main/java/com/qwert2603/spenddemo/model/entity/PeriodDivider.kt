package com.qwert2603.spenddemo.model.entity

data class PeriodDivider(
        val date: SDate,
        val time: STime?,
        val interval: Interval
) : RecordsListItem {
    override val id = date.date * 100L * 100 * 10000 + (time?.time ?: 0)
}