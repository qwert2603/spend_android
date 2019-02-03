package com.qwert2603.spend.model.entity

data class Totals(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val spendsCount: Int,
        val spendsSum: Long,
        val profitsCount: Int,
        val profitsSum: Long
) : RecordsListItem {
    override val id = 1918L

    val balance = profitsSum - spendsSum
}