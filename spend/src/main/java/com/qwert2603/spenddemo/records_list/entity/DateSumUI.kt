package com.qwert2603.spenddemo.records_list.entity

import com.qwert2603.andrlib.util.Const
import java.util.*

data class DateSumUI(
        val date: Date,
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    // fixme: id changes if time changes.
    override val id = date.time / Const.MILLIS_PER_DAY
}