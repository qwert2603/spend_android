package com.qwert2603.spenddemo.records_list.entity

data class TotalsUI(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spendsCount: Int,
        val spendsSum: Long,
        val profitsCount: Int,
        val profitsSum: Long,
        val totalBalance: Long
) : RecordsListItem {
    override val id = 1918L
}