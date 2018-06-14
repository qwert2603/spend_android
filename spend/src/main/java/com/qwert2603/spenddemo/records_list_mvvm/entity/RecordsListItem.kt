package com.qwert2603.spenddemo.records_list_mvvm.entity

import com.qwert2603.andrlib.model.IdentifiableLong

interface RecordsListItem : IdentifiableLong {

    companion object {
        const val ADDENDUM_ID_SPEND = 0L
        const val ADDENDUM_ID_PROFIT = 1_000_000_000L
        private const val ADDENDUM_ID_DATE_SUM = 2_000_000_000L
        private const val ADDENDUM_ID_MONTH_SUM = 3_000_000_000L
        private const val ADDENDUM_ID_TOTALS = 4_000_000_000L
    }

    fun idInList() = when (this) {
        is SpendUI -> this.id + ADDENDUM_ID_SPEND
        is ProfitUI -> this.id + ADDENDUM_ID_PROFIT
        is DateSumUI -> this.id + ADDENDUM_ID_DATE_SUM
        is MonthSumUI -> this.id + ADDENDUM_ID_MONTH_SUM
        is TotalsUI -> this.id + ADDENDUM_ID_TOTALS
        else -> null!!
    }

    fun time() = when (this) {
        is SpendUI -> this.date.time
        is ProfitUI -> this.date.time
        is DateSumUI -> this.date.time
        is MonthSumUI -> this.date.time
        is TotalsUI -> Long.MIN_VALUE
        else -> null!!
    }

    fun priority() = when (this) {
        is SpendUI -> 5
        is ProfitUI -> 4
        is DateSumUI -> 3
        is MonthSumUI -> 2
        is TotalsUI -> 1
        else -> null!!
    }
}