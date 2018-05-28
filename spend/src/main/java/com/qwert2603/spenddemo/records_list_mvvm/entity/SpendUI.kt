package com.qwert2603.spenddemo.records_list_mvvm.entity

import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.entity.SyncStatus
import java.util.*

data class SpendUI(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val syncStatus: SyncStatus,
        val changeKind: ChangeKind?
) : RecordsListItem {
    val canEdit = changeKind != ChangeKind.DELETE
    val canDelete = changeKind != ChangeKind.DELETE
}

fun Spend.toSpendUI(syncStatus: SyncStatus, changeKind: ChangeKind?) = SpendUI(id, kind, value, date, syncStatus, changeKind)
