package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.sync_processor.LocalItem
import java.util.*

@Entity(indices = [
    Index("id", unique = true),
    Index("kind"),
    Index("date"),
    Index("change_id", unique = true),
    Index("change_changeKind")
])
data class SpendTable(
        @PrimaryKey override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        @Embedded(prefix = "change_") override val change: RecordChange?
) : LocalItem

fun SpendTable.toSpend() = Spend(id, kind, value, date)
fun Spend.toSpendTable(change: RecordChange?) = SpendTable(id, kind, value, date, change)