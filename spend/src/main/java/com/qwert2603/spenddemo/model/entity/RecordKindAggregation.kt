package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.model.hashCodeLong

data class RecordKindAggregation(
        val recordTypeId: Long,
        val recordCategory: RecordCategory,
        val kind: String,
        val lastRecord: Record,
        val recordsCount: Int,
        val totalValue: Long
) : IdentifiableLong {
    override val id = recordCategory.uuid.hashCodeLong() + kind.hashCodeLong()
}