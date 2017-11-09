package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.syncprocessor.entity.Identifiable
import java.util.*

data class SyncingRecord(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date
) : Identifiable<Long>

fun SyncingRecord.toRecord() = Record(id, kind, value, date)
fun Record.toSyncingRecord() = SyncingRecord(id, kind, value, date)