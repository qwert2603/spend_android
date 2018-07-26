package com.qwert2603.spenddemo.model.local_db.results

import com.qwert2603.spenddemo.model.entity.ChangeKind
import java.util.*

data class RecordResult(
        val type: Int,
        val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val time: Date?,
        val changeKind: ChangeKind?
) {
    companion object {
        const val TYPE_FAKE = -1
        const val TYPE_PROFIT = 1
        const val TYPE_SPEND = 2
    }
}