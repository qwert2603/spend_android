package com.qwert2603.spenddemo.model.entity

data class SumsShowInfo(
        val showDaySums: Boolean,
        val showMonthSums: Boolean,
        val showYearSums: Boolean,
        val showBalances: Boolean
) {
    companion object {
        val DEFAULT = SumsShowInfo(true, true, true, false)
    }

    fun showDaySumsEnable() = showMonthSums || showYearSums
    fun showMonthSumsEnable() = showDaySums || showYearSums
    fun showYearSumsEnable() = showDaySums || showMonthSums
}