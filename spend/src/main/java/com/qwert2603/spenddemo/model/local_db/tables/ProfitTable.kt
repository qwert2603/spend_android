package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.RecordChange
import java.util.*

@Entity(indices = [
    Index("id", unique = true),
    Index("change_id", unique = true),
    Index("change_changeKind")
])
data class ProfitTable(
        @PrimaryKey val id: Long,
        @ColumnInfo(index = true) val kind: String,
        val value: Int,
        @ColumnInfo(index = true) val date: Date,
        @Embedded(prefix = "change_") val change: RecordChange? = null
)

fun Profit.toProfitTable() = ProfitTable(id, kind, value, date)