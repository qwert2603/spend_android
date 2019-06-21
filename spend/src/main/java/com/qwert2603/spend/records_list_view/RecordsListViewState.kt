package com.qwert2603.spend.records_list_view

import com.qwert2603.spend.model.entity.Record

data class RecordsListViewState(
        val records: List<Record>?,
        val oldRecordsLock: Boolean
) {
    fun canChangeRecords(): Boolean = records?.all { it.isChangeable(oldRecordsLock) } == true
}