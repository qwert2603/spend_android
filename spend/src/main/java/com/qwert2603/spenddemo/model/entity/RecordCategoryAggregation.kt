package com.qwert2603.spenddemo.model.entity

data class RecordCategoryAggregation(
        val recordTypeId: Long,
        val recordCategory: RecordCategory,
        val lastRecord: Record?,
        val recordsCount: Int,
        val totalValue: Long
)