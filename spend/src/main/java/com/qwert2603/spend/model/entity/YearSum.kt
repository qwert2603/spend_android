package com.qwert2603.spend.model.entity

data class YearSum(
        val year: Int, // format is "yyyy"
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spends: Long,
        val profits: Long
) : RecordsListItem {
    override val id = year.toLong()

    val balance = profits - spends
}