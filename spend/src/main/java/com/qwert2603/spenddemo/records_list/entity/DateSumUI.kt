package com.qwert2603.spenddemo.records_list.entity

import java.util.*

data class DateSumUI(
        val date: Date,
        val spends: Int?,//todo: Long?
        val profits: Int?//todo: Long?
) : RecordsListItem {
    override val id = date.time
}