package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

data class RecordsListViewState(
        val records: List<RecordsListItem>,
        val changesCount: Int,
        val showChangeKinds: Boolean,
        val showIds: Boolean,
        val showDateSums: Boolean,
        val showMonthSums: Boolean,
        val showProfits: Boolean,
        val showSpends: Boolean,
        val balance30Days: Long
) {
    fun showSpendsEnable() = showProfits || showDateSums || showMonthSums
    fun showProfitsEnable() = showSpends || showDateSums || showMonthSums
    fun showDateSumsEnable() = showSpends || showProfits || showMonthSums
    fun showMonthSumsEnable() = showSpends || showProfits || showDateSums
    fun newProfitEnable() = showProfits
    fun newSpendVisible() = showSpends
    fun showFloatingDate() = showDateSums && (showSpends || showProfits)
}