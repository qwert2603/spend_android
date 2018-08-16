package com.qwert2603.spenddemo.records_list_mvvm.entity

import java.util.*

// todo: use it.
data class YearSumUI(
        val date: Date,
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = TODO()
}