package com.qwert2603.spend.records_list

data class SumsInfo(
        val longSum: Long?,
        val shortSum: Long?,
        val changesCount: Int?
) {
    companion object {
        val EMPTY = SumsInfo(null, null, null)
    }
}