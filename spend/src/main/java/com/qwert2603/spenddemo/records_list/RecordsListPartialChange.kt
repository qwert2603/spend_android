package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.PartialChange
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

sealed class RecordsListPartialChange : PartialChange {
    data class RecordsListUpdated(val records: List<RecordsListItem>) : RecordsListPartialChange()
    data class ShowChangeKinds(val show: Boolean) : RecordsListPartialChange()
    data class ShowIds(val show: Boolean) : RecordsListPartialChange()
}