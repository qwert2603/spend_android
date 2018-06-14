package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.SpendKind
import java.util.*

@Entity
data class SpendKindTable(
        @PrimaryKey @ColumnInfo(index = true) val kind: String,
        val lastDate: Date,
        val lastPrice: Int,
        val spendsCount: Int
)

fun SpendKindTable.toSpendKind() = SpendKind(kind, spendsCount, lastPrice, lastDate)
fun SpendKind.toSpendKindTable() = SpendKindTable(kind, lastDate, lastPrice, spendsCount)