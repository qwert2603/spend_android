package com.qwert2603.spenddemo.records_list.entity

import java.util.*

data class DateSum(
        val date: Date,
        val sum: Int
) : RecordsListItem {
    override val id = date.time
}