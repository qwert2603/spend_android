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
        val lastTime: Date?,
        val lastPrice: Int,
        val spendsCount: Int
)

fun SpendKindTable.toSpendKind() = SpendKind(
        kind = kind,
        spendsCount = spendsCount,
        lastPrice = lastPrice,
        lastDate = lastDate,
        lastTime = lastTime
)