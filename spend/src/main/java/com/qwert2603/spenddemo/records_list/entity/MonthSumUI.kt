package com.qwert2603.spenddemo.records_list.entity

import java.util.*

data class MonthSumUI(
        val month: Date,
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = month.time
}