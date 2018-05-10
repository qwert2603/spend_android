package com.qwert2603.spenddemo.model.local_db.tables

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.entity.ChangeKind

@Entity
data class ChangeTable(
        val changeKind: ChangeKind,
        @PrimaryKey val spendId: Long
)

fun Change.toChangeTable() = ChangeTable(changeKind, spendId)
fun ChangeTable.toChange() = Change(changeKind, spendId)