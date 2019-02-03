package com.qwert2603.spend.model.entity

data class RecordsFilters(
        val searchQuery: String,
        val startDate: SDate?,
        val endDate: SDate?
) {
    fun check(record: Record) =
            (searchQuery.isEmpty() || record.recordCategory.name.contains(searchQuery, ignoreCase = true) || record.kind.contains(searchQuery, ignoreCase = true))
                    && (startDate == null || record.date >= startDate)
                    && (endDate == null || record.date <= endDate)
}