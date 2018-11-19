package com.qwert2603.spenddemo.records_list

data class ShowInfo(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val showSums: Boolean,
        val showChangeKinds: Boolean,
        val showTimes: Boolean
) {
    fun showSpendSum() = showSpends || !showProfits
    fun showProfitSum() = showProfits || !showSpends
    fun showSpendsEnable() = showProfits || showSums
    fun showProfitsEnable() = showSpends || showSums
    fun showSumsEnable() = showSpends || showProfits
    fun newProfitEnable() = showProfits
    fun newSpendVisible() = showSpends
    fun showFloatingDate() = showSums && (showSpends || showProfits)
    fun showDeleted() = showChangeKinds
}