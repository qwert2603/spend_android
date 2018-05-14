package com.qwert2603.spenddemo.records_list.entity

import com.qwert2603.andrlib.util.Const
import java.util.*

data class MonthSumUI(
        val month: Date,
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = month.time / Const.MILLIS_PER_DAY
}