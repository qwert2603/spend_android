package com.qwert2603.spend.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.model.hashCodeLong

data class RecordCategoryAggregation(
        val recordTypeId: Long,
        val recordCategory: RecordCategory,
        val lastRecord: Record?,
        val recordsCount: Int,
        val totalValue: Long
) : IdentifiableLong {
    override val id = recordCategory.uuid.hashCodeLong()
}