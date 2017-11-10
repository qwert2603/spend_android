package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Record
import java.util.*

@Entity
data class RecordTable(
        @PrimaryKey val id: Long,
        val kind: String,
        val value: Int,
        @ColumnInfo(index = true) val date: Date
)

fun RecordTable.toRecord() = Record(id, kind, value, date)
fun Record.toRecordTable() = RecordTable(id, kind, value, date)