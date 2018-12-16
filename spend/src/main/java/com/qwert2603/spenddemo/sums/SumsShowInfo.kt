package com.qwert2603.spenddemo.sums

data class SumsShowInfo(
        val showDaySums: Boolean,
        val showMonthSums: Boolean,
        val showYearSums: Boolean,
        val showBalances: Boolean
) {
    fun showDaySumsEnable() = showMonthSums || showYearSums
    fun showMonthSumsEnable() = showDaySums || showYearSums
    fun showYearSumsEnable() = showDaySums || showMonthSums

}