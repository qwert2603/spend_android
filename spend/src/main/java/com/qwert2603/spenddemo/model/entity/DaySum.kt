package com.qwert2603.spenddemo.model.entity

data class DaySum(
        val day: Int, // format is "yyyyMMdd"
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = day.toLong()
}