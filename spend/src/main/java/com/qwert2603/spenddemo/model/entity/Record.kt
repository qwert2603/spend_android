package com.qwert2603.spenddemo.model.entity

import java.util.*

data class Record(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date
) : IdentifiableLong