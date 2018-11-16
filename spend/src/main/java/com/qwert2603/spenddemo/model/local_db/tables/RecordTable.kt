package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.rest.entity.RecordServer
import com.qwert2603.spenddemo.model.sync_processor.LocalItem

@Entity(indices = [
    Index("uuid", unique = true),
    Index("date"),
    Index("time"),
    Index("kind"),
    Index("change_id", unique = true),
    Index("change_changeKindId")
])
data class RecordTable(
        @PrimaryKey override val uuid: String,
        val recordTypeId: Long,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int,
        @Embedded(prefix = "change_") override val change: RecordChange?
) : LocalItem

fun RecordTable.toRecordServer() = RecordServer(
        uuid = uuid,
        recordTypeId = recordTypeId,
        date = date,
        time = time,
        kind = kind,
        value = value
)

fun RecordTable.toRecord() = Record(
        uuid = uuid,
        recordTypeId = recordTypeId,
        date = date,
        time = time,
        kind = kind,
        value = value,
        change = change
)