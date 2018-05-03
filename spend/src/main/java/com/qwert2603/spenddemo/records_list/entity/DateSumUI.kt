package com.qwert2603.spenddemo.records_list.entity

import java.util.*

data class DateSumUI(
        val date: Date,
        val spends: Int,
        val profits: Int
) : RecordsListItem {
    override val id = date.time
}