package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong

data class Change(
        override val id: Long,
        val changeKind: ChangeKind,
        val spendId: Long
) : IdentifiableLong