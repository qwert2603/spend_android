package com.qwert2603.spenddemo.records_list.entity

data class TotalsUi(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spendsCount: Int,
        val spendsSum: Int,
        val profitsCount: Int,
        val profitsSum: Int,
        val totalBalance: Int
) : RecordsListItem {
    override val id = 459267892756L
}