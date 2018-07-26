package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.sync_processor.LocalItem
import java.util.*

@Entity(indices = [
    Index("id", unique = true),
    Index("kind"),
    Index("date"),
    Index("time"),
    Index("change_id", unique = true),
    Index("change_changeKind")
])
data class ProfitTable(
        @PrimaryKey override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val time: Date?,
        @Embedded(prefix = "change_") override val change: RecordChange?
) : LocalItem

fun ProfitTable.toProfit() = Profit(id, kind, value, date, time)
fun Profit.toProfitTable(change: RecordChange?) = ProfitTable(id, kind, value, date, time, change)