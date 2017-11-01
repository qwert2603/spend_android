package com.qwert2603.spenddemo.model.entity

import com.qwert2603.spenddemo.utils.hashCodeLong

data class Kind(
        val kind: String
) : IdentifiableLong {
    override val id = kind.hashCodeLong()
}
