package com.qwert2603.spenddemo.records_list_mvvm

data class SumsInfo(
        val longSum: Long?,
        val shortSum: Long?,
        val changesCount: Int?
) {
    companion object {
        val EMPTY = SumsInfo(null, null, null)
    }
}