package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Profit
import java.util.*

@Entity(indices = [Index("id", unique = true)])
class ProfitTable(
        @PrimaryKey val id: Long,
        val kind: String,
        val value: Int,
        @ColumnInfo(index = true) val date: Date
)

fun ProfitTable.toProfit() = Profit(id, kind, value, date)
fun Profit.toProfitTable() = ProfitTable(id, kind, value, date)