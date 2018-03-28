package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.model.hashCodeLong
import java.util.*

data class Kind(
        val kind: String,
        val lastPrice: Int,
        val lastDate: Date
) : IdentifiableLong {
    override val id = kind.hashCodeLong()
}
