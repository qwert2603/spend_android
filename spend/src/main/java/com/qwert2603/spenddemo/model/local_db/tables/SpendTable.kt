package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.Spend
import java.util.*

@Entity(indices = [
    Index("id", unique = true),
    Index("change_id", unique = true),
    Index("change_changeKind")
    //todo:other indices here
])
data class SpendTable(
        @PrimaryKey val id: Long,
        @ColumnInfo(index = true) val kind: String,
        val value: Int,
        @ColumnInfo(index = true) val date: Date,
        @Embedded(prefix = "change_") val change: RecordChange?
)

fun SpendTable.toCreatingSpend() = CreatingSpend(kind, value, date)
fun Spend.toSpendTable(change: RecordChange?) = SpendTable(id, kind, value, date, change)