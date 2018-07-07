package com.qwert2603.spenddemo.records_list_mvvm.entity

import com.qwert2603.spenddemo.model.entity.ChangeKind
import java.util.*

data class ProfitUI(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val changeKind: ChangeKind?
) : RecordsListItem {
    val canEdit = changeKind != ChangeKind.DELETE
    val canDelete = changeKind != ChangeKind.DELETE
}
