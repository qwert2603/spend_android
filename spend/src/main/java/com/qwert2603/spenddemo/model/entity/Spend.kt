package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import java.util.*

data class Spend(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val time: Date?
) : IdentifiableLong