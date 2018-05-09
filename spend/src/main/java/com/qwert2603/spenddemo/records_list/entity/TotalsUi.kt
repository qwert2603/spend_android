package com.qwert2603.spenddemo.records_list.entity

data class TotalsUi(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spendsCount: Int,
        val spendsSum: Long,
        val profitsCount: Int,
        val profitsSum: Long,
        val totalBalance: Long
) : RecordsListItem {
    override val id = 459267892756L
}