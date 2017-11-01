package com.qwert2603.spenddemo.model.entity

data class Change(
        val changeKind: ChangeKind,
        val recordId: Long
) : IdentifiableLong {
    override val id = recordId
}
