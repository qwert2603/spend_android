package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

data class RecordsListViewState(
        val records: List<RecordsListItem>,
        val recordsCount: Int
)