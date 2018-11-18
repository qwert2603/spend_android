package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.model.hashCodeLong

data class RecordKind(
        val recordTypeId: Long,
        val kind: String,
        val lastDate: Int,
        val lastTime: Int?,
        val lastValue: Int,
        val recordsCount: Int
//       todo val totalValue: Long
) : IdentifiableLong {
    override val id = kind.hashCodeLong()
}