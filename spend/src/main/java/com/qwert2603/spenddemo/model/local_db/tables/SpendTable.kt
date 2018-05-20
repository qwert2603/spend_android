package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Spend
import java.util.*

@Entity(indices = [Index("id", unique = true)])
data class SpendTable(
        @PrimaryKey val id: Long,
        val kind: String,
        val value: Int,
        @ColumnInfo(index = true) val date: Date
)

fun SpendTable.toSpend() = Spend(id, kind, value, date)
fun Spend.toSpendTable() = SpendTable(id, kind, value, date)