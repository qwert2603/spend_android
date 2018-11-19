package com.qwert2603.spenddemo.records_list

data class ShowInfo(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val showSums: Boolean,
        val showChangeKinds: Boolean,
        val showTimes: Boolean
) {
    fun showSpendsEnable() = showProfits
    fun showProfitsEnable() = showSpends
    fun newProfitEnable() = showProfits
    fun newSpendVisible() = showSpends
    fun showFloatingDate() = showSums && (showSpends || showProfits)
    fun showDeleted() = showChangeKinds
}