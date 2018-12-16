package com.qwert2603.spenddemo.model.entity

data class MonthSum(
        val month: Int, //format is "yyyyMM"
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = month.toLong()

    val balance = profits - spends
}