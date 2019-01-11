package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SDate

data class Filters(
        val searchQuery: String,
        val startDate: SDate?,
        val endDate: SDate?
) {
    fun check(record: Record) =
            (searchQuery.isEmpty() || record.recordCategory.name.contains(searchQuery, ignoreCase = true) || record.kind.contains(searchQuery, ignoreCase = true))
                    && (startDate == null || record.date >= startDate)
                    && (endDate == null || record.date <= endDate)
}