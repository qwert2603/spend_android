package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

sealed class RecordsListPartialChange : PartialChange {
    data class RecordsListUpdated(val records: List<RecordsListItem>) : RecordsListPartialChange()
    data class ShowChangeKinds(val show: Boolean) : RecordsListPartialChange()
    data class ShowIds(val show: Boolean) : RecordsListPartialChange()
    data class ShowDateSums(val show: Boolean) : RecordsListPartialChange()
    data class ShowSpends(val show: Boolean) : RecordsListPartialChange()
    data class ShowProfits(val show: Boolean) : RecordsListPartialChange()
}