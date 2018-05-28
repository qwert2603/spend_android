package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.SpendChange

@Entity(foreignKeys = [(ForeignKey(
        entity = SpendTable::class,
        parentColumns = ["id"],
        childColumns = ["spendId"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.RESTRICT
))])
data class SpendChangeTable(
        @PrimaryKey val id: Long,
        val changeKind: ChangeKind,
        @ColumnInfo(index = true) val spendId: Long
)

fun SpendChange.toChangeTable() = SpendChangeTable(id, changeKind, spendId)
fun SpendChangeTable.toChange() = SpendChange(id, changeKind, spendId)