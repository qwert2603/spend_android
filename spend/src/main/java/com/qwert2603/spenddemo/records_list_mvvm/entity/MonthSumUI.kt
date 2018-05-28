package com.qwert2603.spenddemo.records_list_mvvm.entity

import com.qwert2603.andrlib.util.Const
import java.util.*

data class MonthSumUI(
        val date: Date,
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = date.time / Const.MILLIS_PER_DAY
}