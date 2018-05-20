package com.qwert2603.spenddemo.model.local_db.results

import java.util.*

data class RecordResult(
        val type: Int,
        val id: Long,
        val kind: String,
        val value: Int,
        val date: Date
)