package com.qwert2603.spenddemo.records_list.entity

import java.util.*

data class DateSumUI(
        val date: Date,
        val spends: Long?,
        val profits: Long?
) : RecordsListItem {
    override val id = date.time
}