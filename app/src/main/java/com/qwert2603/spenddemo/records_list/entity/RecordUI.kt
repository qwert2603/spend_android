package com.qwert2603.spenddemo.records_list.entity

import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SyncStatus
import java.util.*

data class RecordUI(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val syncStatus: SyncStatus,
        val changeKind: ChangeKind?
) : RecordsListItem

fun Record.toRecordUI(syncStatus: SyncStatus, changeKind: ChangeKind?) = RecordUI(id, kind, value, date, syncStatus, changeKind)
fun RecordUI.toRecord() = Record(id, kind, value, date)