package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.entity.ChangeKind

@Entity
data class ChangeTable(
        @PrimaryKey val id: Long,
        val changeKind: ChangeKind,
        @ColumnInfo(index = true) val spendId: Long
)

fun Change.toChangeTable() = ChangeTable(id, changeKind, spendId)
fun ChangeTable.toChange() = Change(id, changeKind, spendId)